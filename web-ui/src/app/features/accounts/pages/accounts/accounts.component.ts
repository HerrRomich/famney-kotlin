import { ChangeDetectionStrategy, Component } from '@angular/core';
import { AccountDto, AccountsApiService } from '@famoney-apis/accounts';
import { NotificationsService } from 'angular2-notifications';
import { catchError, EMPTY, Observable, shareReplay } from 'rxjs';

@Component({
  selector: 'fm-accounts',
  templateUrl: './accounts.component.html',
  styleUrls: ['./accounts.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountsComponent {
  accounts$: Observable<Array<AccountDto>>;

  constructor(private accountsApiService: AccountsApiService, private notificationsService: NotificationsService) {
    this.accounts$ = this.accountsApiService.getAllAccounts().pipe(
      shareReplay(1),
      catchError(() => {
        this.notificationsService.error('Error', "Couldn't load list of accounts.");
        return EMPTY;
      }),
    );;
  }

}
