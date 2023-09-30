import { ChangeDetectionStrategy, Component, DestroyRef, inject, Inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ApiErrorDto, EntryDataDto, EntryItemDataDto } from '@famoney-apis/accounts';
import { EntryItemFormGroup, EntryItemService } from '@famoney-features/accounts/components/entry-item';
import { AccountEntry, EntryItem } from '@famoney-features/accounts/models/account-entry.model';
import { EntryCategoryService, FlatEntryCategoryObject } from '@famoney-shared/services/entry-category.service';
import { NotifierService } from 'angular-notifier';
import { mergeWith, of } from 'rxjs';
import { filter, map, switchMap } from 'rxjs/operators';

@Component({
  selector: 'fm-movement-entry-dialog',
  templateUrl: 'movement-entry-dialog.component.html',
  styleUrls: ['movement-entry-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MovementEntryDialogComponent {
  private formBuilder = inject(NonNullableFormBuilder);

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
  extendedDate = signal<'extended-date' | undefined>(undefined);
  extendedEntry = signal<'extended-entry' | undefined>(undefined);

  constructor(
    private dialogRef: MatDialogRef<MovementEntryDialogComponent, EntryDataDto>,
    private entryCategoriesService: EntryCategoryService,
    private notifierService: NotifierService,
    @Inject(MAT_DIALOG_DATA) private data: EntryDataDto,
    private entryItemService: EntryItemService,
    destroyRef: DestroyRef,
  ) {
    this.dialogRef
      .keydownEvents()
      .pipe(
        filter((event) => event.key === 'Escape'),
        mergeWith(this.dialogRef.backdropClick()),
        takeUntilDestroyed(destroyRef),
      )
      .subscribe(() => this.close());
    of(this.data)
      .pipe(
        switchMap((entryData) =>
          this.entryCategoriesService.entryCategoriesForVisualisation$.pipe(
            map((entryCategories) => {
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
        const {
          entryItems,
          movementDate: { bookingDate, budgetPeriod },
        } = accountEntry;
        this.extendedDate.set(bookingDate || budgetPeriod ? 'extended-date' : undefined);
        this.extendedEntry.set(entryItems.length > 1 ? 'extended-entry' : undefined);
        this.entryForm.setControl(
          'entryItems',
          this.formBuilder.array(entryItems.map(() => this.entryItemService.createEntryItemFormGroup())),
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

  close() {
    this.dialogRef.close();
  }

  addEntryItem() {
    this.entryForm.controls.entryItems.push(this.entryItemService.createEntryItemFormGroup());
  }

  deleteEntryItem(entryItemIndex: number) {
    this.entryForm.controls.entryItems.removeAt(entryItemIndex);
  }

  getEntryDateError() {
    const entryDateControl = this.entryForm.get('entryDate');
    if (entryDateControl?.hasError('matDatepickerParse')) {
      return 'accounts.entryDialog.fields.entryDate.errors.invalid';
    } else if (entryDateControl?.getError('required')) {
      return 'accounts.entryDialog.fields.entryDate.errors.required';
    } else {
      return undefined;
    }
  }

  save() {
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
              const categoryId = typeof entryItem?.categoryId === 'number' ? entryItem?.categoryId : 0;
              const entryCategory = entryCategories.flatEntryCategories.get(categoryId);
              return {
                categoryId,
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
      )
      .subscribe({
        next: (data) => this.dialogRef.close(data),
        error: (err: ApiErrorDto) => this.notifierService.notify('error', err.description ?? ''),
      });
  }
}
