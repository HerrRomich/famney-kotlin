import { Component, Inject, OnInit, Optional } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DATE_LOCALE } from '@angular/material/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { AccountDto } from '@famoney-apis/accounts';
import { EntryDialogData } from '@famoney-features/accounts/models/account-entry.model';
import { nullDate, nullString } from '@famoney-shared/misc';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'fm-account-dialog',
  templateUrl: 'account-dialog.component.html',
  styleUrls: ['account-dialog.component.scss'],
})
export class AccountDialogComponent implements OnInit {
  readonly accountForm = this._formBuilder.group({
    name: [nullString],
    openingDate: [new Date(), [Validators.required]],
    bookingDate: [nullDate],
    budgetPeriod: [nullDate],
    tags: [nullString],
  });
  constructor(
    private dialogRef: MatDialogRef<AccountDialogComponent, AccountDto>,
    private _formBuilder: FormBuilder,
    @Optional() @Inject(MAT_DATE_LOCALE) private dateLocale: string,
    private translateService: TranslateService,
    @Inject(MAT_DIALOG_DATA) private data: EntryDialogData,
  ) {}

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

  submit() {
    console.log('Account submit!');
  }
}
