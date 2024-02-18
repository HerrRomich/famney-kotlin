import { EntryCategoriesDto, EntryCategoryDto } from '@famoney-apis/master-data';
import { StoreOperation } from '@famoney-shared/stores';

export type EntryCategory = {
  id: number;
  type: EntryCategoryDto['type'];
  name: string;
};

export type HierarchicalEntryCategory = {
  id: number;
  type: EntryCategoryDto['type'];
  name: string;
  children: HierarchicalEntryCategory[];
};

export type FlatEntryCategory = EntryCategory & {
  path: string;
  name: string;
  fullPath: string;
  level: number;
  sign: -1 | 1;
};

export type EntryCategoryOperationType = 'createEntryCategory' | 'EntryCategory' | 'EntryCategory';
export type EntryCategoryOperation = StoreOperation<EntryCategoryOperationType>;

export interface EntryCategoriesState {
  entryCategories?: EntryCategoriesDto;
  error?: EntryCategoriesStateError;
  operation?: EntryCategoryOperation;
}

export type EntryCategoriesStateError = {
  readonly message: string;
};

export type FlatEntryCategories = Map<number, FlatEntryCategory>;

export type EntryCategories = {
  flatEntryCategories: FlatEntryCategories;
  expenses: EntryCategory[];
  incomes: EntryCategory[];
};

export const pathSeparator = ' ' + String.fromCharCode(8594) + ' ';
