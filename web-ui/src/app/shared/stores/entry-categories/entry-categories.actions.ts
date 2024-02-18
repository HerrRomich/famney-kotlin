import { EntryCategoriesDto } from '@famoney-apis/master-data';
import { EntryCategoriesStateError } from '@famoney-shared/stores/entry-categories/entry-categories.state';
import { createAction, props } from '@ngrx/store';

export const loadEntryCategories = createAction('[ Entry categories] Load Entry Categories');
export const loadEntryCategoriesSuccess = createAction(
  '[ Entry categories] Load Entry Categories Success',
  props<{
    entryCategories: EntryCategoriesDto;
  }>(),
);
export const loadEntryCategoriesFailure = createAction(
  '[ Entry categories] Load Entry Categories Failure',
  props<{
    error: EntryCategoriesStateError;
  }>(),
);
