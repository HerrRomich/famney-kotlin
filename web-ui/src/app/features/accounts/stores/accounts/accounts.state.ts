import { AccountDto } from '@famoney-apis/accounts';
import { MovementsEntity, MovementsState } from '@famoney-features/accounts/stores/movements/movements.state';
import { EntityState, createEntityAdapter } from '@ngrx/entity';

export type AccountEntity = AccountDto;

export const ACCOUNTS_STATE_OPERATION_TYPE = ['loaded'] as const;

export type AccountsStateOperationType = (typeof ACCOUNTS_STATE_OPERATION_TYPE)[number];

export type AccountsStateOperation = {
  readonly type: AccountsStateOperationType;
  readonly correlationId: string;
};

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

export const movementsAdapter = createEntityAdapter<MovementsEntity>({
  selectId: (movementData) => movementData.pos,
});
