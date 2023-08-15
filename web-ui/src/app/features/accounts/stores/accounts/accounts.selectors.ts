import { ACCOUNTS_FEATURE_KEY } from '@famoney-features/accounts/stores/accounts/accounts.reducer';
import { accountsAdapter, AccountsState } from '@famoney-features/accounts/stores/accounts/accounts.state';
import { createFeatureSelector, createSelector } from '@ngrx/store';

export const selectAccountsState = createFeatureSelector<AccountsState>(ACCOUNTS_FEATURE_KEY);

const { selectAll, selectEntities } = accountsAdapter.getSelectors();

export const allAccountsSelector = createSelector(selectAccountsState, selectAll);

export const allAccountEntitiesSelector = createSelector(selectAccountsState, selectEntities);
export const accountsLoadedSelector = createSelector(selectAccountsState, (state) => state.loaded);

export const allTagsSelector = createSelector(allAccountsSelector, (accounts) => {
  return [...new Set<string>(accounts.flatMap((account) => account.tags ?? []))];
});

export const currentTagsSelector = createSelector(selectAccountsState, (state) => state.selectedTags);
export const currentTagsTextsSelector = createSelector(
  currentTagsSelector,
  (tags) => tags?.map((tag) => '- ' + tag).join('\n'),
);

export const currentTagsCountSelector = createSelector(currentTagsSelector, (tags) => tags?.length);

export const filteredAccountsSelector = createSelector(
  allAccountsSelector,
  currentTagsSelector,
  accountsLoadedSelector,
  (accounts, tags, loaded) =>
    loaded
      ? accounts.filter((account) => !tags || tags.length === 0 || account.tags?.some((tag) => tags.includes(tag)))
      : undefined,
);

export const currentAccountIdSelector = createSelector(selectAccountsState, (state) => state.selectionId);
export const currentAccountSelector = createSelector(
  allAccountEntitiesSelector,
  currentAccountIdSelector,
  (accounts, selectionId) => (selectionId ? accounts[selectionId] : undefined),
);
