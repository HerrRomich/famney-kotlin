import { EntryCategoryDto } from '@famoney-apis/master-data';
import { exclusiveCheck } from '@famoney-shared/misc';
import { createFeatureSelector, createSelector } from '@ngrx/store';
import { ENTRY_CATEGORIES_FEATURE_KEY } from './entry-categories.reducer';
import { EntryCategories, EntryCategoriesState, FlatEntryCategory, pathSeparator } from './entry-categories.state';

export const selectEntryCategoriesState = createFeatureSelector<EntryCategoriesState>(ENTRY_CATEGORIES_FEATURE_KEY);

export const selectEntryCategories = createSelector(
  selectEntryCategoriesState,
  ({ entryCategories }): EntryCategories => {
    const flatEntryCategories = new Map<number, FlatEntryCategory>();
    if (entryCategories) {
      flattenEntryCategories(flatEntryCategories, entryCategories.expenses);
      flattenEntryCategories(flatEntryCategories, entryCategories.incomes);
    }
    return {
      flatEntryCategories: flatEntryCategories,
      incomes: entryCategories?.incomes ?? [],
      expenses: entryCategories?.expenses ?? [],
    };
  },
);

const flattenEntryCategories = (
  result: Map<number, FlatEntryCategory>,
  entryCategories?: EntryCategoryDto[],
  level = 1,
  path = '',
) => {
  entryCategories
    ?.filter((entryCategory) => entryCategory.id)
    .forEach((entryCategory) => {
      const fullPath = (path ? path + pathSeparator : '') + entryCategory.name;
      result.set(entryCategory.id, {
        ...entryCategory,
        path,
        fullPath,
        level,
        sign: getCategorySign(entryCategory.type),
      });
      flattenEntryCategories(result, entryCategory.children, level + 1, fullPath);
    });
};

const getCategorySign = (type: EntryCategoryDto['type']): -1 | 1 => {
  switch (type) {
    case 'EXPENSE':
      return -1;
    case 'INCOME':
      return 1;
    default:
      return exclusiveCheck(type);
  }
};
