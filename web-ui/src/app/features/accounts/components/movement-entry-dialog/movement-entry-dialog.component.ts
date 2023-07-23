import { Component, Inject, OnInit, Optional } from '@angular/core';
import { AbstractControl, FormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { MAT_DATE_LOCALE } from '@angular/material/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { AccountsApiService, ApiErrorDto, EntryDataDto, EntryItemDataDto, MovementDto } from '@famoney-apis/accounts';
import { AccountEntry, EntryDialogData, EntryItem } from '@famoney-features/accounts/models/account-entry.model';
import { nullDate, nullNumber, nullString } from '@famoney-shared/misc';
import { EntryCategoryService, FlatEntryCategoryObject } from '@famoney-shared/services/entry-category.service';
import { DateFormatName, LocaleService } from '@famoney-shared/services/locale.service';
import { ParseNumberService } from '@famoney-shared/services/parse-numbers.service';
import { TranslateService } from '@ngx-translate/core';
import { NotificationsService } from 'angular2-notifications';
import { EMPTY, Observable, of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';

@Component({
  selector: 'fm-movement-entry-dialog',
  templateUrl: 'movement-entry-dialog.component.html',
  styleUrls: ['movement-entry-dialog.component.scss'],
})
export class MovementEntryDialogComponent implements OnInit {
  readonly entryForm = this._formBuilder.group({
    entryDate: [new Date(), [Validators.required]],
    bookingDate: [nullDate],
    budgetPeriod: [nullDate],
    entryItems: this._formBuilder.array([this.addEntryItemFormGroup()]),
  });
  accountEntry$: Observable<AccountEntry>;
  comulatedSum$: Observable<{ amount: number }> = EMPTY;
  extendedDate: string | undefined;
  extendedEntry: string | undefined;

  constructor(
    private dialogRef: MatDialogRef<MovementEntryDialogComponent, MovementDto>,
    private _formBuilder: FormBuilder,
    private _accountsApiService: AccountsApiService,
    private entryCategoriesService: EntryCategoryService,
    @Optional() @Inject(MAT_DATE_LOCALE) private dateLocale: string,
    private translateService: TranslateService,
    private notificationsService: NotificationsService,
    @Inject(MAT_DIALOG_DATA) private data: EntryDialogData,
    private localeService: LocaleService,
    private parseNumberService: ParseNumberService,
  ) {
    this.accountEntry$ = of(this.data.entryData).pipe(
      switchMap(entryData =>
        this.entryCategoriesService.entryCategoriesForVisualisation$.pipe(
          map(entryCategories => [entryData, entryCategories] as const),
        ),
      ),
      tap(([entryData]) => {
        this.extendedDate = entryData?.bookingDate || entryData?.budgetPeriod ? 'extended-date' : undefined;
        this.extendedEntry = (entryData?.entryItems?.length ?? 0) > 1 ? 'extended-entry' : undefined;
      }),
      map(([entryData, entryCategories]) => {
        const accountEntry: AccountEntry = {
          movementDate: {
            date: entryData?.date ?? new Date(),
            bookingDate: entryData?.bookingDate,
            budgetPeriod: entryData?.budgetPeriod,
          },
          entryItems: entryData
            ? entryData.entryItems.map(entryItem =>
                this.createEntryItem(entryItem, entryCategories.flatEntryCategories.get(entryItem.categoryId)),
              )
            : [],
        };
        return accountEntry;
      }),
      tap(accountEntry => {
        if (accountEntry.entryItems.length > 1) {
          this.entryForm.setControl(
            'entryItems',
            this._formBuilder.array(accountEntry.entryItems.map(() => this.addEntryItemFormGroup())),
          );
        }
        this.entryForm.patchValue(accountEntry);
      }),
    );
  }

  getEntryItems() {
    return this.entryForm?.controls['entryItems'];
  }

  private addEntryItemFormGroup() {
    return this._formBuilder.group({
      categoryId: [nullNumber, Validators.required],
      amount: [nullNumber, [Validators.required, this.validateAmountNotZero]],
      comments: [nullString],
    });
  }

  private createEntryItem(entryItem: EntryItemDataDto, flatEntryCategory?: FlatEntryCategoryObject): EntryItem {
    const entryItemAmount = entryItem?.amount;
    const sign = flatEntryCategory?.getCategorySign();
    const amount = entryItemAmount && sign ? entryItemAmount * sign : 0;
    return {
      categoryId: entryItem.categoryId,
      amount: amount,
      comments: entryItem.comments,
    };
  }

  ngOnInit(): void {
    this.dialogRef.keydownEvents().subscribe(event => {
      if (event.key === 'Escape') {
        this.onCancel();
      }
    });
    this.dialogRef.backdropClick().subscribe(() => {
      this.onCancel();
    });
  }

  onCancel() {
    this.dialogRef.close();
  }

  getEntryDate(dateFormatName: DateFormatName) {
    const entryDateControl = this.entryForm?.get('entryDate');
    return entryDateControl?.value ? this.localeService.formatDate(entryDateControl?.value, dateFormatName) : '';
  }

  addEntryItem() {
    this.entryForm?.controls['entryItems'].push(this.addEntryItemFormGroup());
  }

  deleteEntryItem(entryItemIndex: number) {
    this.entryForm?.controls['entryItems'].removeAt(entryItemIndex);
  }

  validateAmountNotZero = (control: AbstractControl): ValidationErrors => {
    const amount = this.parseNumberService.parse(control.value);
    if (Number.isNaN(amount)) {
      return { wrongFormat: 'Should be number!' };
    } else if (amount === 0) {
      return { zeroValue: 'Should be not Zero!' };
    } else {
      return {};
    }
  };

  getEntryDateError$() {
    const entryDateControl = this.entryForm?.get('entryDate');
    if (entryDateControl?.hasError('matDatepickerParse')) {
      return this.translateService.get('accounts.entryDialog.fields.entryDate.errors.invalid');
    } else if (entryDateControl?.getError('required')) {
      return this.translateService.get('accounts.entryDialog.fields.entryDate.errors.required');
    } else {
      return EMPTY;
    }
  }

  submit() {
    const { accountId, movementId } = this.data;
    let storeOperator =
      typeof movementId === 'undefined'
        ? (entryData: EntryDataDto) => this._accountsApiService.addMovement(accountId, entryData)
        : (entryData: EntryDataDto) => this._accountsApiService.changeMovement(accountId, movementId, entryData);
    of(this.entryForm?.value)
      .pipe(
        switchMap(accountEntry =>
          this.entryCategoriesService.entryCategoriesForVisualisation$.pipe(
            map(entryCategories => [accountEntry, entryCategories] as const),
          ),
        ),
        map(([accountEntry, entryCategories]) => {
          const entryItems = accountEntry.entryItems?.map(entryItem => {
            const entryCategory = entryItem?.categoryId ? entryCategories.flatEntryCategories.get(entryItem?.categoryId) : undefined;
            return {
              categoryId: entryItem.categoryId ?? 0,
              amount: (entryCategory?.getCategorySign() ?? 0) * (entryItem?.amount ?? 0),
              comments: entryItem.comments ?? undefined,
            };
          }) ?? [];
          const entry: EntryDataDto = {
            type: 'ENTRY',
            date: accountEntry.entryDate ?? new Date(),
            bookingDate: accountEntry.bookingDate ?? undefined,
            budgetPeriod: accountEntry.budgetPeriod?? undefined,
            entryItems: entryItems,
            amount: entryItems.reduce((amount, entryItem) => amount + entryItem.amount, 0),
          };
          return entry;
        }),
        switchMap(storeOperator),
        tap(data => {
          this.dialogRef.close(data);
        }),
        catchError((err: ApiErrorDto) => {
          this.notificationsService.error('Error', err.description);
          return EMPTY;
        }),
      )
      .subscribe();
  }
}
