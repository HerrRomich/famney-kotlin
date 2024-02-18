import { MovementDto } from '@famoney-apis/accounts';
import { DateRange } from '@famoney-features/accounts/stores/accounts/accounts.state';
import { StoreOperation } from '@famoney-shared/stores';
import { createEntityAdapter, EntityState } from '@ngrx/entity';
import { Range } from 'multi-integer-range';

export type MovementOperationType = 'createMovement' | 'updateMovement' | 'deleteMovement';
export type MovementOperation = StoreOperation<MovementOperationType>;

export interface MovementsState extends EntityState<MovementDto> {
  readonly dateRange: DateRange;
  readonly count: number;
  readonly movementsRange?: Range;
  error?: MovementsStateError;
  operation?: MovementOperation;
}

export type MovementsStateError = {
  readonly message: string;
};
