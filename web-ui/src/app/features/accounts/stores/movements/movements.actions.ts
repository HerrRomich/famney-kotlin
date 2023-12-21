import { MovementDataDto, MovementDto } from '@famoney-apis/accounts';
import {
  MovementOperation, MovementsEntity,
  MovementsEntityEntry,
  MovementsStateError
} from '@famoney-features/accounts/stores/movements/movements.state';
import { StoreOperation } from '@famoney-shared/stores';
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
export const createMovement = createAction(
  '[ Movements] Create Movement',
  props<{
    movementType: MovementDataDto['type'];
    operation: StoreOperation<'createMovement'>;
  }>(),
);
export const updateMovement = createAction(
  '[ Movements] Update Movement',
  props<{
    pos: number;
    operation: StoreOperation<'updateMovement'>;
  }>(),
);
export const deleteMovement = createAction(
  '[ Movements] Delete Movement',
  props<{
    pos: number;
    operation: StoreOperation<'deleteMovement'>;
  }>(),
);
export const storeMovementSuccess = createAction(
  '[ Movements] Store Movement Success',
  props<{ pos?: number; entity?: MovementsEntity; operation: MovementOperation }>(),
);
export const storeMovementCanceled = createAction(
  '[ Movements] Store Movement Canceled',
  props<{ operation: MovementOperation }>(),
);
export const storeMovementFailure = createAction(
  '[ Movements] Store Movement Failure',
  props<{
    error: MovementsStateError;
    operation: MovementOperation;
  }>(),
);
