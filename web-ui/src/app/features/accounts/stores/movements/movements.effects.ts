import { inject, Injectable } from '@angular/core';
import { AccountsApiService, MovementDto } from '@famoney-apis/accounts';
import { MovementsService } from '@famoney-features/accounts/services/movements.service';
import * as AccountsActions from '@famoney-features/accounts/stores/accounts/accounts.actions';
import * as AccountsSelectors from '@famoney-features/accounts/stores/accounts/accounts.selectors';
import * as MovementsActions from '@famoney-features/accounts/stores/movements/movements.actions';
import * as MovementsSelectors from '@famoney-features/accounts/stores/movements/movements.selectors';
import { MovementsEntityEntry } from '@famoney-features/accounts/stores/movements/movements.state';
import { EntryCategoryService } from '@famoney-shared/services/entry-category.service';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { multirange } from 'multi-integer-range';
import { of, OperatorFunction, pipe, switchMap, withLatestFrom } from 'rxjs';
import { catchError, combineLatestWith, map } from 'rxjs/operators';

@Injectable()
export class MovementsEffects {
  private readonly store = inject(Store);
  private readonly actions$ = inject(Actions);
  private readonly accountsApiService = inject(AccountsApiService);
  private readonly movementsService = inject(MovementsService);
  private readonly entryCategoriesService = inject(EntryCategoryService);

  readonly selectAccount$ = createEffect(() =>
    this.actions$.pipe(
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
          return this.getLoadFailure(`Invalid request: ${request.toString()}`);
        }
        return this.accountsApiService.getMovements(currentAccount.id, undefined, undefined, min, max - min + 1).pipe(
          this.mapMovementsDtoTEntityData(),
          map((loadedMovements) => {
            return MovementsActions.loadMovementsRangeSuccess({
              requestedRange,
              loadedRange: request,
              loadedMovements,
            });
          }),
          catchError((error) => this.getLoadFailure(error.message ?? 'Loading data failed!')),
        );
      }),
    ),
  );

  private getLoadFailure(message: string) {
    return of(
      MovementsActions.loadMovementsRangeFailure({
        error: {
          message,
        },
      }),
    );
  }

  private mapMovementsDtoTEntityData(): OperatorFunction<MovementDto[], MovementsEntityEntry[]> {
    return pipe(
      combineLatestWith(this.entryCategoriesService.entryCategoriesForVisualisation$),
      map(([movements, entryCategories]) =>
        movements.map<MovementsEntityEntry>((movement) => {
          const categoryId = this.movementsService.getEntryItemData(movement)?.categoryId;
          const category = categoryId ? entryCategories.flatEntryCategories.get(categoryId) : undefined;
          return {
            movement,
            category,
          };
        }),
      ),
    );
  }
}
