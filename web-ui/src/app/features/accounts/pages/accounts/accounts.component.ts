import { ChangeDetectionStrategy, Component } from '@angular/core';
import { AccountsStore } from '@famoney-features/accounts/store/accounts.store';

@Component({
  selector: 'fm-accounts',
  templateUrl: './accounts.component.html',
  styleUrls: ['./accounts.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountsComponent {
  constructor(public accountsStore: AccountsStore) {
  }
}
