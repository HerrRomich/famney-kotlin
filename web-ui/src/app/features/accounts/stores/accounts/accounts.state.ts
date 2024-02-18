import { AccountDto } from '@famoney-apis/accounts';
import { MovementsState } from '@famoney-features/accounts/stores/movements/movements.state';
import { StoreOperation } from '@famoney-shared/stores';
import { createEntityAdapter, EntityState } from '@ngrx/entity';

export type AccountEntity = AccountDto;

export const ACCOUNTS_STATE_OPERATION_TYPE = ['loaded'] as const;

export type AccountsStateOperationType = (typeof ACCOUNTS_STATE_OPERATION_TYPE)[number];

export type AccountsStateOperation = StoreOperation<AccountsStateOperationType>;

export type AccountsStateError = {
  message: string;
};

export interface AccountsState extends EntityState<AccountEntity> {
  readonly loaded: boolean;
  readonly error?: AccountsStateError;
  readonly selectedTags: string[];
  readonly selectionId?: number;
  readonly operation?: AccountsStateOperation;
}

export type DateRange = {
  readonly start?: Date;
  readonly end?: Date;
};

export type AccountSelection = {
  readonly account: AccountDto;
  readonly movements: MovementsState;
};

export const accountsAdapter = createEntityAdapter<AccountEntity>({
  selectId: (account) => account.id,
  sortComparer: (account1, account2) => account1.name.localeCompare(account2.name),
});
