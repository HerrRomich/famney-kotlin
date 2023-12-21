import { inject, Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from '@famoney-shared/components/conformation-dialog/confirmation-dialog.component';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable()
export class ConfirmationDialogService {
  private readonly dialog = inject(MatDialog);

  query(message: string): Observable<boolean> {
    const dialogRef = this.dialog.open<ConfirmationDialogComponent, string, boolean>(ConfirmationDialogComponent, {
      data: message,
    });
    return dialogRef.afterClosed().pipe(map((result): result is boolean => !!result));
  }
}
