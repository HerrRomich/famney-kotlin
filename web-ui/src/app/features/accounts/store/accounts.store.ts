import { Injectable } from '@angular/core';
import { AccountDto, AccountsApiService, MovementDto } from '@famoney-apis/accounts';
import { ComponentStore, OnStoreInit } from '@ngrx/component-store';
import { createEntityAdapter, DictionaryNum, EntityState } from '@ngrx/entity';
import { createSelector } from '@ngrx/store';
import { NotificationsService } from 'angular2-notifications';
import { multirange, Range } from 'multi-integer-range';
import { EMPTY, Observable } from 'rxjs';
import { takeUntil } from 'rxjs/internal/operators/takeUntil';
import { catchError, filter, map, switchMap, tap } from 'rxjs/operators';

const ACCOUNTS_FILTER_STORAGE = 'ACCOUNTS_FILTER_STORAGE';
const ACCOUNT_ID_STORAGE = 'ACCOUNT_ID_STORAGE';

const getAllTags = (accounts: AccountDto[]): string[] => {
  return accounts.reduce(
    (tags, account) => (account.tags ? [...tags, ...account.tags.filter(tag => !tags.includes(tag))] : tags),
    Array<string>(),
  );
};

export type MovementData = {
  readonly pos: number;
  readonly movement: MovementDto | null;
};

type DateRange = {
  readonly start: Date | null;
  readonly end: Date | null;
};

export type AccountsFilter = {
  readonly selectedTags: string[] | null;
  readonly dateRange: DateRange | null;
};

export interface MovementsState extends EntityState<MovementData> {
  readonly loadedData: Range | null;
}

export type AccountSelection = {
  readonly account: AccountDto;
  readonly movements: MovementsState;
};

export type AccountsState = {
  readonly filter: AccountsFilter;
  readonly accounts: EntityState<AccountDto>;
  readonly accountSelection: AccountSelection | null;
};

const accountsAdapter = createEntityAdapter<AccountDto>();
const accountsSelector = (state: AccountsState) => state.accounts;
const accountsIdsSelector = createSelector(accountsSelector, accounts => <number[]>accounts.ids);
const accountsEntitiesSelector = createSelector(
  accountsSelector,
  accounts => <DictionaryNum<AccountDto>>accounts.entities,
);
const allAccountsSelector = createSelector(accountsIdsSelector, accountsEntitiesSelector, (ids, entities) =>
  ids.map(id => <AccountDto>entities[id]),
);
const accountsFilterSelector = createSelector(
  (state: AccountsState) => state,
  (state: AccountsState) => state.filter,
);
const currentTagsSelector = createSelector(accountsFilterSelector, filter => filter.selectedTags);
const filteredAccountsSelector = createSelector(allAccountsSelector, currentTagsSelector, (accounts, tags) =>
  accounts.filter(account => tags === null || tags.length === 0 || account.tags?.some(tag => tags.includes(tag))),
);
const allTagsSelector = createSelector(allAccountsSelector, accounts => getAllTags(accounts));

const selectionSelector = (state: AccountsState) => state.accountSelection;
const currentAccountSelector = createSelector(selectionSelector, selection => selection?.account ?? null);
const currentAccountIdSelector = createSelector(selectionSelector, selection => selection?.account?.id ?? null);
const currentLoadedDataSelector = createSelector(selectionSelector, selection => selection?.account ?? null);
const movementsAdapter = createEntityAdapter({
  selectId: (data: MovementData) => data.pos,
});
const movementsSelector = createSelector(selectionSelector, selection => selection?.movements ?? null);
const movementsIdsSelector = createSelector(movementsSelector, movements =>
  movements ? <number[]>movements.ids : null,
);
const movementsEntitiesSelector = createSelector(movementsSelector, movements =>
  movements ? <DictionaryNum<MovementData>>movements.entities : null,
);
const allMovementsSelector = createSelector(movementsIdsSelector, movementsEntitiesSelector, (ids, entities) =>
  ids && entities ? ids.map(id => <MovementData>entities[id]) : [],
);
const movementsLoadedDataSelector = createSelector(movementsSelector, movementsState => movementsState?.loadedData);

@Injectable()
export class AccountsStore extends ComponentStore<AccountsState> implements OnStoreInit {
  readonly accounts$ = this.select(allAccountsSelector);
  readonly accountIds$ = this.select(accountsIdsSelector).pipe(map(ids => <number[]>ids));
  readonly filteredAccounts$ = this.select(filteredAccountsSelector);
  readonly filter$ = this.select(accountsFilterSelector);
  readonly tags$ = this.select(allTagsSelector);
  readonly selectedTags$ = this.select(currentTagsSelector);
  readonly movements$ = this.select(allMovementsSelector);
  readonly currentAccountId$ = this.select(currentAccountIdSelector);
  readonly currentLoadedData$ = this.select(movementsLoadedDataSelector);

  constructor(private _accountsApiService: AccountsApiService, private _notificationsService: NotificationsService) {
    super();
  }

  ngrxOnStoreInit() {
    this._accountsApiService
      .getAllAccounts()
      .pipe(
        tap(accounts => {
          const allTags = getAllTags(accounts);
          const filter = this.loadFilter();
          filter.selectedTags?.filter(tag => allTags.includes(tag)).sort();
          this.setState({
            accounts: accountsAdapter.setAll(
              accounts.sort((a, b) => a.id - b.id),
              accountsAdapter.getInitialState(),
            ),
            accountSelection: null,
            filter,
          });
        }),
        catchError(e => {
          this._notificationsService.error('Error', "Couldn't load list of accounts.");
          return EMPTY;
        }),
      )
      .subscribe();
    this.filter$.pipe(tap(this.storeFilter), takeUntil(this.destroy$)).subscribe();
  }

  private storeFilter(filter: AccountsFilter) {
    window.localStorage.setItem(ACCOUNTS_FILTER_STORAGE, JSON.stringify(filter));
  }

  readonly loadAccounts = this.effect<void>(trigger$ =>
    trigger$.pipe(
      switchMap(() =>
        this._accountsApiService.getAllAccounts().pipe(
          tap(accounts =>
            this.patchState(state => ({
              accounts: accountsAdapter.setAll(
                accounts.sort((a, b) => a.id - b.id),
                state.accounts,
              ),
            })),
          ),
          catchError(e => {
            this._notificationsService.error('Error', "Couldn't load list of accounts.");
            return EMPTY;
          }),
        ),
      ),
    ),
  );

  private loadFilter() {
    try {
      const filter = <AccountsFilter>JSON.parse(window.localStorage.getItem(ACCOUNTS_FILTER_STORAGE) ?? '');
      const selectedTags =
        Array.isArray(filter?.selectedTags) && filter?.selectedTags?.every(val => typeof val === 'string')
          ? filter?.selectedTags
          : null;
      const dateRange: DateRange | null =
        typeof filter.dateRange === 'object'
          ? <DateRange>{
              start: filter.dateRange?.start instanceof Date ? filter.dateRange.start : null,
              end: filter.dateRange?.end instanceof Date ? filter.dateRange.start : null,
            }
          : null;

      const result: AccountsFilter = {
        selectedTags: selectedTags,
        dateRange: dateRange?.start || dateRange?.end ? dateRange : null,
      };
      return result;
    } catch {}
    return <AccountsFilter>{};
  }

  addTagToSelection(tag: string) {
    const state = this.get();
    const filter = state.filter;
    const selectedTags = filter.selectedTags ?? [];
    if (selectedTags.indexOf(tag) > -1) {
      return;
    }
    this.patchState({ filter: { ...filter, selectedTags: [...selectedTags, tag].sort() } });
  }

  removeTagFromSelection(tag: string) {
    const state = this.get();
    const filter = state.filter;
    const selectedTags = filter.selectedTags ?? [];
    const pos = selectedTags.indexOf(tag);
    if (pos === -1) {
      return;
    }
    this.patchState({
      filter: { ...filter, selectedTags: [...selectedTags.slice(0, pos), ...selectedTags.slice(pos + 1)].sort() },
    });
  }

  clearSelectedTags() {
    this.patchState(state => ({ filter: { ...state.filter, selectedTags: null } }));
  }

  readonly selectAccount = this.effect((accountId$: Observable<number>) =>
    accountId$.pipe(
      switchMap(accountId => this._accountsApiService.getAccount(accountId)),
      switchMap((account: AccountDto) => {
        const currState = this.get();
        return this._accountsApiService
          .getMovementsCount(
            account.id,
            currState.filter.dateRange?.start ?? undefined,
            currState.filter.dateRange?.end ?? undefined,
          )
          .pipe(map(count => [account, count] as const));
      }),
      tap(([account, count]) =>
        this.patchState(state => {
          const movements = {
            ...movementsAdapter.setAll(
              Array.from({ length: count }, (_, pos) => ({ pos, movement: null })),
              state.accountSelection?.movements ?? movementsAdapter.getInitialState({}),
            ),
            loadedData: null,
          };
          const selection: AccountSelection = {
            account,
            movements,
          };
          return {
            accountSelection: selection,
          };
        }),
      ),
    ),
  );

  readonly loadData = this.effect((range$: Observable<Range>) =>
    range$.pipe(
      switchMap(range => {
        const currState = this.get();
        const accountSelection = currState.accountSelection;
        if (!accountSelection) {
          return EMPTY;
        }
        const ids = accountSelection.movements.ids;
        const dataRange = ids && ids.length > 0 ? multirange([[0, ids.length - 1]]) : multirange();
        const requestedRange = multirange([range]);
        const currRange = accountSelection.movements.loadedData
          ? multirange([accountSelection.movements.loadedData])
          : multirange();
        const request = requestedRange.clone().intersect(dataRange).subtract(currRange);
        const min = request.min();
        const max = request.max();
        if (min !== undefined && max !== undefined) {
          return this._accountsApiService
            .getMovements(
              accountSelection.account.id,
              currState.filter.dateRange?.start ?? undefined,
              currState.filter.dateRange?.end ?? undefined,
              min,
              max - min + 1,
            )
            .pipe(
              map(loadedMovements =>
                this.patchState(state => {
                  const loadedRange = multirange([[min, max]]);
                  const positionsToUpdate = currRange.subtract(requestedRange).append(loadedRange);
                  const updates = positionsToUpdate.toArray().map(pos => ({
                    id: pos,
                    changes: {
                      pos,
                      movement: loadedRange.has(pos) ? loadedMovements[pos - min] : undefined,
                    },
                  }));
                  return {
                    ...state,
                    accountSelection: {
                      ...accountSelection,
                      movements: {
                        ...movementsAdapter.updateMany(updates, accountSelection.movements),
                        loadedData: range,
                      },
                    },
                  };
                }),
              ),
            );
        }
        return EMPTY;
      }),
    ),
  );
}
