import { EntryCategoriesState } from '@famoney-shared/stores/entry-categories/entry-categories.state';
import { Action, createReducer, on } from '@ngrx/store';
import * as entryCategoriesActions from './entry-categories.actions';

export const ENTRY_CATEGORIES_FEATURE_KEY = 'entryCategories';

const initEntryCategories = (): EntryCategoriesState => ({});

const reducer = createReducer(
  initEntryCategories(),
  on(entryCategoriesActions.loadEntryCategoriesSuccess, (state, { entryCategories }) => ({
    entryCategories,
    ...state,
  })),
  on(entryCategoriesActions.loadEntryCategoriesFailure, (state, { error }) => ({
    ...state,
    error,
  })),
);

export function entryCategoriesReducer(state: EntryCategoriesState | undefined, action: Action) {
  return reducer(state, action);
}
