import { AccountDto } from '@famoney-apis/accounts';
import { AccountsStateError } from '@famoney-features/accounts/stores/accounts/accounts.state';
import { createAction, props } from '@ngrx/store';

export const initAccounts = createAction('[ Accounts ] Init Accounts');
export const loadAccounts = createAction('[ Accounts ] Load Accounts');
export const loadAccountsSuccess = createAction(
  'Load Accounts Success',
  props<{
    selectedTags: string[];
    accounts: AccountDto[];
  }>(),
);
export const loadAccountsFailure = createAction(
  '[ Accounts ] Load Accounts Failure',
  props<{ error: AccountsStateError }>(),
);
export const selectAccount = createAction('[ Accounts ] Select Account', props<{ selectionId: number }>());
export const addTagToSelection = createAction('[ Accounts ] Add Tag To Selection', props<{ tag: string }>());
export const removeTagFromSelection = createAction('[ Accounts ] Remove Tag From Selection', props<{ tag: string }>());
export const clearSelectedTags = createAction('[ Accounts ] Clear Selected Tags');
export const changeTagsSuccess = createAction('Change Accounts Tags Success', props<{ selectedTags: string[] }>());
export const changeTagsFailure = createAction('Change Accounts Tags Failure', props<{ error: AccountsStateError }>());
export const selectAccountSuccess = createAction(
  'Select Account Success',
  props<{
    selectionId: number;
  }>(),
);
export const selectAccountsFailure = createAction('Select Account Failure', props<{ error: AccountsStateError }>());
