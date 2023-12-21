import { inject, Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { EntryDataDto } from '@famoney-apis/accounts';
import { MovementEntryDialogComponent } from '@famoney-features/accounts/components/movement-entry-dialog';
import { Observable } from 'rxjs';

@Injectable()
export class MovementDialogService {
  private readonly accountEntryDialogComponent = inject(MatDialog);

  constructor() {}

  createMovementEntry() {
    return this.openMovementEntryDialog();
  }

  editMovementEntry(entryData: EntryDataDto) {
    return this.openMovementEntryDialog(entryData);
  }

  private openMovementEntryDialog(data?: EntryDataDto): Observable<EntryDataDto | undefined> {
    const accountEntryDialogRef = this.accountEntryDialogComponent.open<
      MovementEntryDialogComponent,
      EntryDataDto,
      EntryDataDto
    >(MovementEntryDialogComponent, {
      width: 'min(100vw, max(60vw, 400px))',
      maxWidth: '100vw',
      maxHeight: '100vh',
      panelClass: 'fm-account-entry-dialog',
      disableClose: true,
      hasBackdrop: true,
      data,
    });
    return accountEntryDialogRef.afterClosed();
  }
}
