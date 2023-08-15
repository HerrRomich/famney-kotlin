import { Injectable } from '@angular/core';
import { z } from 'zod';

const ACCOUNTS_SELECTED_TAGS_STORAGE = 'accounts.selected-tags';
const ACCOUNT_ID_STORAGE = 'ACCOUNT_ID_STORAGE';

const accountsSelectedTagsSchema = z.string().array().optional();

@Injectable()
export class AccountsFilterStorageService {
  storeAccountsSelectedTags(selectedTags: string[]) {
    window.localStorage.setItem(ACCOUNTS_SELECTED_TAGS_STORAGE, JSON.stringify(selectedTags));
  }

  restoreAccountsSelectedTags(): string[] {
    try {
      return (
        accountsSelectedTagsSchema.parse(
          JSON.parse(window.localStorage.getItem(ACCOUNTS_SELECTED_TAGS_STORAGE) ?? ''),
        ) ?? []
      );
    } catch {
      return [];
    }
  }
}
