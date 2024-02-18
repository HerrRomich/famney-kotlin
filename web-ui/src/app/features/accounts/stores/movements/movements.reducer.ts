import { MovementDto } from '@famoney-apis/accounts';
import { createEntityAdapter } from '@ngrx/entity';
import { Action, createReducer, on } from '@ngrx/store';
import * as movementsActions from './movements.actions';
import { MovementsState } from './movements.state';

export const MOVEMENTS_FEATURE_KEY = 'movements';

export const movementsAdapter = createEntityAdapter<MovementDto>({
  selectId: (movement) => movement.id,
});

const initMovements = (): MovementsState =>
  movementsAdapter.getInitialState({
    dateRange: {},
    count: 0,
  });

const reducer = createReducer(
  initMovements(),
  on(movementsActions.selectAccount, (state, { count }) =>
    movementsAdapter.removeAll({
      ...state,
      count,
      movementsRange: undefined,
    }),
  ),
  on(movementsActions.loadMovementsRangeSuccess, (state, { movementsRange, movements }) => {
    return movementsAdapter.setAll(movements, {
      ...state,
      movementsRange,
    });
  }),
  on(movementsActions.loadMovementsRangeFailure, (state, { error }) => ({
    ...state,
    error,
  })),
  on(movementsActions.storeMovementSuccess, (state, { movement, operation }) => {
    if (movement !== undefined) {
      state = movementsAdapter.removeOne(movement.id, state);
      state = movementsAdapter.addOne(movement, state);
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
