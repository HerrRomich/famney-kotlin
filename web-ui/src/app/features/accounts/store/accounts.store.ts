import { Injectable } from '@angular/core';
import { AccountDto, AccountsApiService } from '@famoney-apis/accounts';
import { ComponentStore, OnStoreInit } from '@ngrx/component-store';
import { createEntityAdapter, EntityState } from '@ngrx/entity';
import { createSelector } from '@ngrx/store';
import { NotificationsService } from 'angular2-notifications';
import { EMPTY, Observable } from 'rxjs';
import { takeUntil } from 'rxjs/internal/operators/takeUntil';
import { catchError, map, switchMap, tap } from 'rxjs/operators';

const ACCOUNT_TAGS_STORAGE = 'ACCOUNT_TAGS_STORAGE';
const ACCOUNT_ID_STORAGE = 'ACCOUNT_ID_STORAGE';

const getAllTags = (accounts: AccountDto[]): string[] => {
  return accounts.reduce(
    (tags, account) => (account.tags ? [...tags, ...account.tags.filter(tag => !tags.includes(tag))] : tags),
    Array<string>(),
  );
};

export interface AccountsState extends EntityState<AccountDto> {
  selectedTags: string[];
}

const adapter = createEntityAdapter<AccountDto>();
const { selectAll: allAccountsSelector, selectIds: allAccountIdsSelector } = adapter.getSelectors();
const currentTagsSelector = createSelector(
  (state: AccountsState) => state.selectedTags,
  tags => tags,
);
const filteredAccountsSelector = createSelector(allAccountsSelector, currentTagsSelector, (accounts, tags) =>
  accounts.filter(account => tags.length == 0 || account.tags?.some(tag => tags.includes(tag))),
);
const allTagsSelector = createSelector(allAccountsSelector, accounts => getAllTags(accounts));

@Injectable()
export class AccountsStore extends ComponentStore<AccountsState> implements OnStoreInit {
  readonly accounts$ = this.select(allAccountsSelector);
  readonly accountIds$ = this.select(allAccountIdsSelector).pipe(map(ids => <number[]>ids));
  readonly filteredAccounts$ = this.select(filteredAccountsSelector);
  readonly tags$ = this.select(allTagsSelector);
  readonly selectedTags$ = this.select(currentTagsSelector);

  constructor(private _accountsApiService: AccountsApiService, private _notificationsService: NotificationsService) {
    super();
  }

  ngrxOnStoreInit() {
    this._accountsApiService
      .getAllAccounts()
      .pipe(
        tap(accounts => {
          const allTags = getAllTags(accounts);
          const selectedTags = this.loadSelectedTags().filter(tag =>allTags.includes(tag)).sort();
          this.setState({
            ...adapter.setAll(
              accounts.sort((a, b) => a.id - b.id),
              adapter.getInitialState(),
            ),
            selectedTags,
          });
        }),
        catchError(e => {
          this._notificationsService.error('Error', "Couldn't load list of accounts.");
          return EMPTY;
        }),
      )
      .subscribe();
    this.selectedTags$.pipe(tap(this.storeSelectedTags), takeUntil(this.destroy$)).subscribe();
  }

  private storeSelectedTags(selectedTags: string[]) {
    window.localStorage.setItem(ACCOUNT_TAGS_STORAGE, JSON.stringify(selectedTags));
  }

  readonly loadAccounts = this.effect<void>(trigger$ =>
    trigger$.pipe(
      switchMap(() =>
        this._accountsApiService.getAllAccounts().pipe(
          tap(accounts =>
            this.patchState(state =>
              adapter.setAll(
                accounts.sort((a, b) => a.id - b.id),
                state,
              ),
            ),
          ),
          catchError(e => {
            this._notificationsService.error('Error', "Couldn't load list of accounts.");
            return EMPTY;
          }),
        ),
      ),
    ),
  );

  private loadSelectedTags() {
    try {
      const selectedTags = JSON.parse(window.localStorage.getItem(ACCOUNT_TAGS_STORAGE) ?? '[]');
      if (Array.isArray(selectedTags) && selectedTags.every(val => typeof val === 'string')) {
        return selectedTags as string[];
      } else {
        return Array<string>();
      }
    } catch {
      return Array<string>();
    }
  }

  addTagToSelection(tag: string) {
    this.patchState(state => {
      if (state.selectedTags.indexOf(tag) !== -1) {
        return state;
      }
      return { ...state, selectedTags: [...state.selectedTags, tag].sort() };
    });
  }

  removeTagFromSelection(tag: string) {
    this.patchState(state => {
      const selectedTags = state.selectedTags;
      const pos = selectedTags.indexOf(tag);
      if (pos === -1) {
        return state;
      }
      return { ...state, selectedTags: [...state.selectedTags.slice(0, pos), ...selectedTags.slice(pos + 1)].sort() };
    });
  }

  clearSelectedTags() {
    this.patchState(state => ({ ...state, selectedTags: [] }));
  }
}
