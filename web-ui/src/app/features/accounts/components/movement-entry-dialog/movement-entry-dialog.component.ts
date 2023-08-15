import { Component, DestroyRef, Inject, OnInit, Optional, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, Validators } from '@angular/forms';
import { MAT_DATE_LOCALE } from '@angular/material/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { AccountsApiService, ApiErrorDto, EntryDataDto, EntryItemDataDto, MovementDto } from '@famoney-apis/accounts';
import { EntryItemFormGroup, EntryItemService } from '@famoney-features/accounts/components/entry-item';
import { AccountEntry, EntryDialogData, EntryItem } from '@famoney-features/accounts/models/account-entry.model';
import { EntryCategoryService, FlatEntryCategoryObject } from '@famoney-shared/services/entry-category.service';
import { DateFormatName, LocaleService } from '@famoney-shared/services/locale.service';
import { TranslateService } from '@ngx-translate/core';
import { NotifierService } from 'angular-notifier';
import { EMPTY, of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

@Component({
  selector: 'fm-movement-entry-dialog',
  templateUrl: 'movement-entry-dialog.component.html',
  styleUrls: ['movement-entry-dialog.component.scss'],
})
export class MovementEntryDialogComponent implements OnInit {
  readonly entryForm = this.formBuilder.group({
    entryDate: this.formBuilder.control(new Date(), [Validators.required]),
    bookingDate: this.formBuilder.control<Date | undefined>({
      value: undefined,
      disabled: false,
    }),
    budgetPeriod: this.formBuilder.control<Date | undefined>({
      value: undefined,
      disabled: false,
    }),
    entryItems: this.formBuilder.array<EntryItemFormGroup>([]),
  });
  cumulatedSum = signal<number | undefined>(undefined);
  loaded = signal<boolean>(false);
  extendedDate: string | undefined;
  extendedEntry: string | undefined;

  constructor(
    private dialogRef: MatDialogRef<MovementEntryDialogComponent, MovementDto>,
    private formBuilder: NonNullableFormBuilder,
    private accountsApiService: AccountsApiService,
    private entryCategoriesService: EntryCategoryService,
    @Optional() @Inject(MAT_DATE_LOCALE) private dateLocale: string,
    private translateService: TranslateService,
    private notifierService: NotifierService,
    @Inject(MAT_DIALOG_DATA) private data: EntryDialogData,
    private localeService: LocaleService,
    private entryItemService: EntryItemService,
    private destroyRef: DestroyRef,
  ) {}

  ngOnInit(): void {
    this.dialogRef
      .keydownEvents()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((event) => {
        if (event.key === 'Escape') {
          this.onCancel();
        }
      });
    this.dialogRef
      .backdropClick()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.onCancel();
      });
    of(this.data.entryData)
      .pipe(
        switchMap((entryData) =>
          this.entryCategoriesService.entryCategoriesForVisualisation$.pipe(
            map((entryCategories) => {
              this.extendedDate = entryData?.bookingDate || entryData?.budgetPeriod ? 'extended-date' : undefined;
              this.extendedEntry = (entryData?.entryItems?.length ?? 0) > 1 ? 'extended-entry' : undefined;
              const accountEntry: AccountEntry = {
                movementDate: {
                  date: entryData?.date ?? new Date(),
                  bookingDate: entryData?.bookingDate,
                  budgetPeriod: entryData?.budgetPeriod,
                },
                entryItems: entryData
                  ? entryData.entryItems.map((entryItem) =>
                      this.createEntryItem(entryItem, entryCategories.flatEntryCategories.get(entryItem.categoryId)),
                    )
                  : [],
              };
              return accountEntry;
            }),
          ),
        ),
      )
      .subscribe((accountEntry) => {
        this.entryForm.setControl(
          'entryItems',
          this.formBuilder.array(accountEntry.entryItems.map(() => this.entryItemService.createEntryItemFormGroup())),
        );
        this.entryForm.patchValue(accountEntry);
        this.loaded.set(true);
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

  onCancel() {
    this.dialogRef.close();
  }

  getEntryDate(dateFormatName: DateFormatName) {
    const entryDateControl = this.entryForm?.get('entryDate');
    return entryDateControl?.value ? this.localeService.formatDate(entryDateControl?.value, dateFormatName) : '';
  }

  addEntryItem() {
    this.entryForm.controls.entryItems.push(this.entryItemService.createEntryItemFormGroup());
  }

  deleteEntryItem(entryItemIndex: number) {
    this.entryForm.controls.entryItems.removeAt(entryItemIndex);
  }

  getEntryDateError$() {
    const entryDateControl = this.entryForm.get('entryDate');
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
        ? (entryData: EntryDataDto) => this.accountsApiService.addMovement(accountId, entryData)
        : (entryData: EntryDataDto) => this.accountsApiService.changeMovement(accountId, movementId, entryData);
    of(this.entryForm?.value)
      .pipe(
        switchMap((accountEntry) =>
          this.entryCategoriesService.entryCategoriesForVisualisation$.pipe(
            map((entryCategories) => [accountEntry, entryCategories] as const),
          ),
        ),
        map(([accountEntry, entryCategories]) => {
          const entryItems =
            accountEntry.entryItems?.map((entryItem) => {
              const entryCategory = entryItem?.categoryId
                ? entryCategories.flatEntryCategories.get(entryItem?.categoryId)
                : undefined;
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
            budgetPeriod: accountEntry.budgetPeriod ?? undefined,
            entryItems: entryItems,
            amount: entryItems.reduce((amount, entryItem) => amount + entryItem.amount, 0),
          };
          return entry;
        }),
        switchMap(storeOperator),
      )
      .subscribe({
        next: (data) => this.dialogRef.close(data),
        error: (err: ApiErrorDto) => this.notifierService.notify('error', err.description ?? ''),
      });
  }
}
