import * as AccountsActions from '@famoney-features/accounts/stores/accounts/accounts.actions';
import { accountsAdapter, AccountsState } from '@famoney-features/accounts/stores/accounts/accounts.state';
import { Action, createReducer, on } from '@ngrx/store';

export const ACCOUNTS_FEATURE_KEY = 'accounts';

const initAccounts = (): AccountsState =>
  accountsAdapter.getInitialState({
    loaded: false,
    selectedTags: [],
  });

const reducer = createReducer(
  initAccounts(),
  on(AccountsActions.loadAccounts, (state) => ({
    ...state,
    loaded: false,
  })),
  on(AccountsActions.loadAccountsSuccess, (state, { selectedTags, accounts }) =>
    accountsAdapter.setAll(accounts, {
      ...state,
      loaded: true,
      selectedTags,
    }),
  ),
  on(AccountsActions.loadAccountsFailure, AccountsActions.changeTagsFailure, (state, { error }) => ({
    ...state,
    error,
  })),
  on(AccountsActions.changeTagsSuccess, (state, { selectedTags }) => ({
    ...state,
    selectedTags,
  })),
  on(AccountsActions.loadAccountsSuccess, (state, { selectedTags, accounts }) =>
    accountsAdapter.setAll(accounts, {
      ...state,
      loaded: true,
      selectedTags,
    }),
  ),
  on(AccountsActions.selectAccountSuccess, (state, { selectionId }) => ({
    ...state,
    selectionId,
  })),
  on(
    AccountsActions.loadAccountsFailure,
    AccountsActions.changeTagsFailure,
    AccountsActions.selectAccountsFailure,
    (state, { error }) => ({
      ...state,
      error,
    }),
  ),
);

export function accountsReducer(state: AccountsState | undefined, action: Action) {
  return reducer(state, action);
}
