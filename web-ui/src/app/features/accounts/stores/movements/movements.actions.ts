import { MovementDataDto, MovementDto } from '@famoney-apis/accounts';
import { MovementOperation, MovementsStateError } from '@famoney-features/accounts/stores/movements/movements.state';
import { StoreOperation } from '@famoney-shared/stores';
import { createAction, props } from '@ngrx/store';
import { Range } from 'multi-integer-range';

export const selectAccount = createAction(
  '[ Movements] Select Account',
  props<{
    count: number;
  }>(),
);
export const loadMovementsRange = createAction(
  '[ Movements] Load Movements',
  props<{
    movementsRange: Range;
  }>(),
);
export const loadMovementsRangeSuccess = createAction(
  '[ Movements] Load Movements Range Success',
  props<{
    movementsRange: Range;
    movements: MovementDto[];
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
    id: number;
    operation: StoreOperation<'updateMovement'>;
  }>(),
);
export const deleteMovement = createAction(
  '[ Movements] Delete Movement',
  props<{
    id: number;
    operation: StoreOperation<'deleteMovement'>;
  }>(),
);
export const storeMovementSuccess = createAction(
  '[ Movements] Store Movement Success',
  props<{ movement?: MovementDto; operation: MovementOperation }>(),
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
