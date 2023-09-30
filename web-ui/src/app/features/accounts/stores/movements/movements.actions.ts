import { EntryDataDto, MovementDataDto } from '@famoney-apis/accounts';
import { MovementsEntityEntry, MovementsStateError } from '@famoney-features/accounts/stores/movements/movements.state';
import { createAction, props } from '@ngrx/store';
import { MultiRange, Range } from 'multi-integer-range';

export const initMovements = createAction('[ Movements] Init Movements');
export const selectAccount = createAction(
  '[ Movements] Select Account',
  props<{
    count: number;
  }>(),
);
export const loadMovementsRange = createAction(
  '[ Movements] Load Movements',
  props<{
    range: Range | undefined;
  }>(),
);
export const loadMovementsRangeSuccess = createAction(
  '[ Movements] Load Movements Range Success',
  props<{
    requestedRange: MultiRange;
    loadedRange: MultiRange;
    loadedMovements: MovementsEntityEntry[];
  }>(),
);
export const loadMovementsRangeFailure = createAction(
  '[ Movements] Load Movements Range Failure',
  props<{
    error: MovementsStateError;
  }>(),
);
export const addMovementEntry = createAction('[ Movements] Add Movement Entry');
export const editMovementEntry = createAction(
  '[ Movements] Edit Movement Entry',
  props<{
    id: number;
    entryData: EntryDataDto;
  }>(),
);
export const deleteMovement = createAction(
  '[ Movements] Delete Movement',
  props<{
    id: number;
  }>(),
);
export const storeMovement = createAction(
  '[ Movements] Store Movement',
  props<{
    accountId: number;
    movementId?: number;
    movementData?: MovementDataDto;
  }>(),
);
export const storeMovementSuccess = createAction('[ Movements] Store Movement Success');
export const storeMovementFailure = createAction(
  '[ Movements] Store Movement Failure',
  props<{
    error: MovementsStateError;
  }>(),
);
