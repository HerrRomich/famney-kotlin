import { inject, Injectable } from '@angular/core';
import * as AccountsActions from '@famoney-features/accounts/stores/accounts/accounts.actions';
import * as accountsSelector from '@famoney-features/accounts/stores/accounts/accounts.selectors';
import { Store } from '@ngrx/store';
import { defer } from 'rxjs';

@Injectable()
export class AccountsFacade {
  private store = inject(Store);

  readonly allAccountEntities$ = defer(() => {
    this.init();
    return this.store.select(accountsSelector.allAccountEntitiesSelector);
  });
  readonly filteredAccounts$ = this.store.select(accountsSelector.filteredAccountsSelector);
  readonly allTags$ = this.store.select(accountsSelector.allTagsSelector);
  readonly selectedTags$ = this.store.select(accountsSelector.currentTagsSelector);
  readonly tagsTexts$ = this.store.select(accountsSelector.currentTagsTextsSelector);
  readonly tagsCount$ = this.store.select(accountsSelector.currentTagsCountSelector);
  readonly currentAccountId$ = this.store.select(accountsSelector.currentAccountIdSelector);

  init() {
    this.store.dispatch(AccountsActions.initAccounts());
  }

  addTagToSelection(tag: string) {
    this.store.dispatch(AccountsActions.addTagToSelection({ tag }));
  }

  removeTagFromSelection(tag: string) {
    this.store.dispatch(AccountsActions.removeTagFromSelection({ tag }));
  }

  clearSelectedTags() {
    this.store.dispatch(AccountsActions.clearSelectedTags());
  }

  selectAccount(selectionId: number) {
    this.store.dispatch(AccountsActions.selectAccount({ selectionId }));
  }
}
