import { inject, Injectable } from '@angular/core';
import { AccountsApiService } from '@famoney-apis/accounts';
import * as AccountsActions from '@famoney-features/accounts/stores/accounts/accounts.actions';
import * as AccountsSelectors from '@famoney-features/accounts/stores/accounts/accounts.selectors';
import * as MovementsActions from '@famoney-features/accounts/stores/movements/movements.actions';
import * as MovementsSelectors from '@famoney-features/accounts/stores/movements/movements.selectors';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { multirange } from 'multi-integer-range';
import { of, switchMap, withLatestFrom } from 'rxjs';
import { map, tap } from 'rxjs/operators';

@Injectable()
export class MovementsEffects {
  private readonly store = inject(Store);
  private readonly actions$ = inject(Actions);
  private readonly accountsApiService = inject(AccountsApiService);

  readonly selectAccount$ = createEffect(() =>
    this.actions$.pipe(
      tap((value) => console.log(JSON.stringify(value))),
      ofType(AccountsActions.selectAccountSuccess, AccountsActions.selectAccountsFailure),
      withLatestFrom(
        this.store.select(AccountsSelectors.currentAccountSelector),
        this.store.select(MovementsSelectors.selectDateRange),
      ),
      switchMap(([, account, dateRange]) => {
        if (!account) {
          return of(MovementsActions.loadMovementsRangeFailure({ error: { message: 'Account is not selected' } }));
        }
        return this.accountsApiService
          .getMovementsCount(account.id, dateRange.start ?? undefined, dateRange.end ?? undefined)
          .pipe(map((count) => MovementsActions.selectAccount({ count })));
      }),
    ),
  );

  readonly loadMovementsRange$ = createEffect(() =>
    this.actions$.pipe(
      ofType(MovementsActions.loadMovementsRange),
      withLatestFrom(
        this.store.select(AccountsSelectors.currentAccountSelector),
        this.store.select(MovementsSelectors.selectMovementsIds),
        this.store.select(MovementsSelectors.selectMovementsRange),
      ),
      switchMap(([{ range }, currentAccount, movementsIds, currentRange]) => {
        if (currentAccount === undefined) {
          return of(MovementsActions.loadMovementsRangeFailure({ error: { message: 'Account is not selected!' } }));
        }
        const dataRange =
          movementsIds && movementsIds.length > 0 ? multirange([[0, movementsIds.length - 1]]) : multirange();
        const requestedRange = multirange(range ? [range] : undefined);
        const request = requestedRange.clone().intersect(dataRange).subtract(currentRange);
        const min = request.min();
        const max = request.max();
        if (min === undefined || max === undefined) {
          return of(
            MovementsActions.loadMovementsRangeFailure({
              error: {
                message: `Invalid request!`,
              },
            }),
          );
        }
        return this.accountsApiService
          .getMovements(currentAccount.id, undefined, undefined, min, max - min + 1)
          .pipe(
            map((loadedMovements) =>
              MovementsActions.loadMovementsRangeSuccess({ requestedRange, loadedRange: request, loadedMovements }),
            ),
          );
      }),
    ),
  );
}
