import { Component, DestroyRef, inject, Inject, Optional } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, Validators } from '@angular/forms';
import { MAT_DATE_LOCALE } from '@angular/material/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { AccountDto } from '@famoney-apis/accounts';

@Component({
  selector: 'fm-account-dialog',
  templateUrl: 'account-dialog.component.html',
  styleUrls: ['account-dialog.component.scss'],
})
export class AccountDialogComponent {
  private dialogRef: MatDialogRef<AccountDialogComponent, AccountDto> = inject(
    MatDialogRef<AccountDialogComponent, AccountDto>,
  );
  private formBuilder = inject(NonNullableFormBuilder);

  readonly accountForm = this.formBuilder.group({
    name: this.formBuilder.control<string | undefined>({ value: undefined, disabled: false }),
    openingDate: this.formBuilder.control<Date>(new Date(), [Validators.required]),
    bookingDate: this.formBuilder.control<Date | undefined>({ value: undefined, disabled: false }),
    budgetPeriod: this.formBuilder.control<Date | undefined>({ value: undefined, disabled: false }),
    tags: this.formBuilder.control<string | undefined>({ value: undefined, disabled: false }),
  });

  constructor(
    @Optional() @Inject(MAT_DATE_LOCALE) private dateLocale: string,
    @Inject(MAT_DIALOG_DATA) private data: AccountDto,
    destroyRef: DestroyRef,
  ) {
    this.dialogRef
      .keydownEvents()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe((event) => {
        if (event.key === 'Escape') {
          this.onCancel();
        }
      });
    this.dialogRef
      .backdropClick()
      .pipe(takeUntilDestroyed(destroyRef))
      .subscribe(() => {
        this.onCancel();
      });
  }

  onCancel() {
    this.dialogRef.close();
  }

  submit() {
    console.log('Account submit!');
  }
}
