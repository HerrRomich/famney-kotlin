import { Injectable } from '@angular/core';
import { AccountDto, AccountsApiService, MovementDto } from '@famoney-apis/accounts';
import { ComponentStore } from '@ngrx/component-store';
import { createEntityAdapter, EntityState, Update } from '@ngrx/entity';
import { multirange, MultiRange, Range } from 'multi-integer-range';
import { combineLatest, EMPTY, filter, map, Observable, pairwise, startWith, switchMap, tap } from 'rxjs';
import { AccountsStore } from './accounts.store';

export interface MovementData {
  readonly pos: number;
  readonly movement?: MovementDto;
}

export interface MovementsState extends EntityState<MovementData> {
  accountId?: number;
  loadedData: MultiRange;
}

const adapter = createEntityAdapter({
  selectId: (data: MovementData) => data.pos,
});

const { selectAll: movementsSelector } = adapter.getSelectors();

@Injectable()
export class MovementsStore extends ComponentStore<MovementsState> {
  readonly movements$ = this.select(movementsSelector);
  readonly accountId$ = this.select((state) => state.accountId);
  readonly loadedData$ = this.select((state) => state.loadedData);

  constructor(private _accountsApiService: AccountsApiService, private _accountsStore: AccountsStore) {
    super(
      adapter.getInitialState({
        loadedData: multirange(),
      }),
    );
  }

  readonly selectAccount = this.effect((accountId$: Observable<number>) =>
    accountId$.pipe(
      switchMap(accountId => this._accountsApiService.getAccount(accountId)),
      tap((account: AccountDto) =>
        this.patchState(state => ({
          ...adapter.setAll(
            Array.from({length: account.movementCount}, (_, pos) => ({ pos })),
            state,
          ),
          accountId: account.id,
          loadedData: multirange(),
        })),
      ),
    ),
  );

  readonly loadData = this.effect((range$: Observable<Range>) =>
    range$.pipe(
      switchMap(range => {
        const currState = this.get();
        const dataRange = currState.ids.length > 0 ? multirange([[0, currState.ids.length - 1]]) : multirange();
        const request = multirange([range]).intersect(dataRange).subtract(currState.loadedData);
        const min = request.min();
        const max = request.max();
        if (currState.accountId !== undefined && min !== undefined && max !== undefined) {
          return this._accountsApiService.getMovements(currState.accountId, min, max - min + 1).pipe(
            map(movements =>
              this.patchState(state => {
                const updates = movements.map<Update<MovementData>>((movement, pos) => ({
                  id: min + pos,
                  changes: {
                    pos: min + pos,
                    movement,
                  },
                }));
                return {
                  ...adapter.updateMany(updates, state),
                  loadedData: state.loadedData.append(multirange([[min, max]])),
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
