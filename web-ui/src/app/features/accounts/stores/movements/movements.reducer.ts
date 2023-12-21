import { movementsAdapter } from '@famoney-features/accounts/stores/accounts/accounts.state';
import * as movementsActions from '@famoney-features/accounts/stores/movements/movements.actions';
import { MovementsEntity, MovementsState } from '@famoney-features/accounts/stores/movements/movements.state';
import { Update } from '@ngrx/entity';
import { Action, createReducer, on } from '@ngrx/store';
import { multirange } from 'multi-integer-range';

export const MOVEMENTS_FEATURE_KEY = 'movements';

const initMovements = (): MovementsState =>
  movementsAdapter.getInitialState({
    dateRange: {},
    movementsRange: multirange(),
  });

const reducer = createReducer(
  initMovements(),
  on(movementsActions.selectAccount, (state, { count }) =>
    movementsAdapter.setAll(
      Array.from({ length: count }, (_, pos) => ({ pos })),
      {
        ...state,
        movementsRange: multirange(),
      },
    ),
  ),
  on(movementsActions.loadMovementsRangeSuccess, (state, { requestedRange, loadedRange, loadedMovements }) => {
    const min = requestedRange.min();
    if (min === undefined) {
      return {
        ...state,
        error: {
          message: 'Request is invalid!',
        },
      };
    }
    const currRange = state.movementsRange.clone();
    const positionsToUpdate = currRange.subtract(requestedRange).append(loadedRange);
    const updates = positionsToUpdate.toArray().map<Update<MovementsEntity>>((pos) => ({
      id: pos,
      changes: {
        pos,
        entry: loadedRange.has(pos) ? loadedMovements[pos - min] : undefined,
      },
    }));
    return movementsAdapter.updateMany(updates, {
      ...state,
      loadedRange: requestedRange,
    });
  }),
  on(movementsActions.loadMovementsRangeFailure, (state, { error }) => ({
    ...state,
    error,
  })),
  on(movementsActions.storeMovementSuccess, (state, { pos, entity, operation }) => {
    if (pos) {
      state = movementsAdapter.removeOne(pos, state);
    }
    if (entity) {
      state = movementsAdapter.addOne(entity, state);
    }
    return {
      ...state,
      operation,
    };
  }),
);

export function movementsReducer(state: MovementsState | undefined, action: Action) {
  return reducer(state, action);
}
