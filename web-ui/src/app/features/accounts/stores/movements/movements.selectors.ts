import { movementsAdapter } from '@famoney-features/accounts/stores/accounts/accounts.state';
import { MOVEMENTS_FEATURE_KEY } from '@famoney-features/accounts/stores/movements/movements.reducer';
import { MovementsState } from '@famoney-features/accounts/stores/movements/movements.state';
import { createFeatureSelector, createSelector } from '@ngrx/store';

export const selectMovementsState = createFeatureSelector<MovementsState>(MOVEMENTS_FEATURE_KEY);

const { selectAll, selectIds, selectEntities } = movementsAdapter.getSelectors();

export const selectAllMovements = createSelector(selectMovementsState, selectAll);
export const selectAllMovementEntities = createSelector(selectMovementsState, selectEntities);
export const selectMovementsIds = createSelector(selectMovementsState, selectIds);
export const selectMovementsRange = createSelector(selectMovementsState, (state) => state.movementsRange);
export const selectDateRange = createSelector(selectMovementsState, (state) => state.dateRange);
export const selectOperation = createSelector(selectMovementsState, (state) => state.operation);
