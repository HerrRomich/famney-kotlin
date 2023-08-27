import { MovementsEntityEntry, MovementsStateError } from '@famoney-features/accounts/stores/movements/movements.state';
import { createAction, props } from '@ngrx/store';
import { MultiRange, Range } from 'multi-integer-range';

export const initMovements = createAction('[ Movements] Init Movements');
export const selectAccount = createAction('[ Movements] Select Account', props<{ count: number }>());
export const loadMovementsRange = createAction('[ Movements] Load Movements', props<{ range: Range | undefined }>());
export const loadMovementsRangeSuccess = createAction(
  '[ Movements] Load Movements Range Success',
  props<{ requestedRange: MultiRange; loadedRange: MultiRange; loadedMovements: MovementsEntityEntry[] }>(),
);
export const loadMovementsRangeFailure = createAction(
  '[ Movements] Load Movements Range Failure',
  props<{ error: MovementsStateError }>(),
);
