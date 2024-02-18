import { inject, Injectable } from '@angular/core';
import { AccountsApiService } from '@famoney-apis/accounts';
import { MovementDialogService } from '@famoney-features/accounts/services/movement-dialog.service';
import * as AccountsActions from '@famoney-features/accounts/stores/accounts/accounts.actions';
import * as AccountsSelectors from '@famoney-features/accounts/stores/accounts/accounts.selectors';
import * as MovementsActions from '@famoney-features/accounts/stores/movements/movements.actions';
import * as MovementsSelectors from '@famoney-features/accounts/stores/movements/movements.selectors';
import { MovementOperation } from '@famoney-features/accounts/stores/movements/movements.state';
import { ConfirmationDialogService } from '@famoney-shared/services/confirmation-dialog.service';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { TranslateService } from '@ngx-translate/core';
import { NotifierService } from 'angular-notifier';
import { concatMap, firstValueFrom, of, switchMap, withLatestFrom } from 'rxjs';
import { fromPromise } from 'rxjs/internal/observable/innerFrom';
import { catchError, map } from 'rxjs/operators';

@Injectable()
export class MovementsEffects {
  private readonly store = inject(Store);
  private readonly actions$ = inject(Actions);
  private readonly accountsApiService = inject(AccountsApiService);
  private readonly movementDialogService = inject(MovementDialogService);
  private readonly confirmationDialogService = inject(ConfirmationDialogService);
  private readonly notifierService = inject(NotifierService);
  private readonly translateService = inject(TranslateService);

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
      withLatestFrom(this.store.select(AccountsSelectors.currentAccountSelector)),
      switchMap(([{ movementsRange }, currentAccount]) => {
        if (currentAccount === undefined) {
          return of(MovementsActions.loadMovementsRangeFailure({ error: { message: 'Account is not selected!' } }));
        }
        const [min, max] = movementsRange;
        return this.accountsApiService.readMovements(currentAccount.id, undefined, undefined, min, max - min + 1).pipe(
          map((movements) =>
            MovementsActions.loadMovementsRangeSuccess({
              movementsRange,
              movements,
            }),
          ),
          catchError((error) => this.getLoadFailure(error.message ?? 'Loading data failed!')),
        );
      }),
    ),
  );

  private getLoadFailure(message: string) {
    return of(MovementsActions.loadMovementsRangeFailure({ error: { message } }));
  }

  readonly createMovement$ = createEffect(() =>
    this.actions$.pipe(
      ofType(MovementsActions.createMovement),
      withLatestFrom(this.store.select(AccountsSelectors.currentAccountIdSelector)),
      concatMap(([{ movementType, operation }, accountId]) => {
        if (!accountId) {
          return fromPromise(this.getInternalFailure(operation, 'No account is selected!'));
        }
        const createMovementData$ =
          movementType === 'ENTRY' ? this.movementDialogService.createMovementEntry() : of(undefined);
        return createMovementData$.pipe(
          concatMap((movementData) =>
            movementData
              ? this.accountsApiService.createMovement(accountId, movementData).pipe(
                  map((movement) =>
                    MovementsActions.storeMovementSuccess({
                      movement,
                      operation,
                    }),
                  ),
                )
              : of(MovementsActions.storeMovementCanceled({ operation })),
          ),
          catchError((error) =>
            of(
              MovementsActions.storeMovementFailure({
                error: { message: error.message ?? 'Failure storing movement!' },
                operation,
              }),
            ),
          ),
        );
      }),
    ),
  );

  readonly updateMovement$ = createEffect(() =>
    this.actions$.pipe(
      ofType(MovementsActions.updateMovement),
      withLatestFrom(
        this.store.select(AccountsSelectors.currentAccountIdSelector),
        this.store.select(MovementsSelectors.selectAllMovementEntities),
      ),
      concatMap(([{ id, operation }, accountId, movements]) => {
        const movement = movements[id];
        if (!accountId) {
          return fromPromise(this.getInternalFailure(operation, 'No account is selected!'));
        }
        if (!movement) {
          return fromPromise(this.getInternalFailure(operation, 'No movement is selected!'));
        }
        const updatedMovementData$ =
          movement.data.type === 'ENTRY' ? this.movementDialogService.editMovementEntry(movement?.data) : of(undefined);
        return updatedMovementData$.pipe(
          concatMap((updatedMovementData) =>
            updatedMovementData
              ? this.accountsApiService.updateMovement(accountId, movement.id, updatedMovementData).pipe(
                  map((movement) =>
                    MovementsActions.storeMovementSuccess({
                      movement,
                      operation,
                    }),
                  ),
                )
              : of(MovementsActions.storeMovementCanceled({ operation })),
          ),
          catchError((error) =>
            of(
              MovementsActions.storeMovementFailure({
                error: { message: error.message ?? 'Failure storing movement!' },
                operation,
              }),
            ),
          ),
        );
      }),
    ),
  );

  readonly deleteMovement$ = createEffect(() =>
    this.actions$.pipe(
      ofType(MovementsActions.deleteMovement),
      withLatestFrom(
        this.store.select(AccountsSelectors.currentAccountIdSelector),
        this.store.select(MovementsSelectors.selectAllMovementEntities),
      ),
      concatMap(([{ id, operation }, accountId, movements]) => {
        if (!accountId) {
          return fromPromise(this.getInternalFailure(operation, 'No account is selected!'));
        }
        const movement = movements[id];
        if (!movement) {
          return fromPromise(this.getInternalFailure(operation, 'No movement is selected!'));
        }
        return this.confirmationDialogService.query('').pipe(
          concatMap((confirmed) =>
            confirmed
              ? this.accountsApiService.deleteMovement(accountId, movement.id).pipe(
                  map(() =>
                    MovementsActions.storeMovementSuccess({
                      movement,
                      operation,
                    }),
                  ),
                )
              : of(MovementsActions.storeMovementCanceled({ operation })),
          ),
          catchError((error) =>
            of(
              MovementsActions.storeMovementFailure({
                error: { message: error.message ?? 'Failure storing movement!' },
                operation,
              }),
            ),
          ),
        );
      }),
    ),
  );

  private async getInternalFailure(operation: MovementOperation, message: string) {
    const errorMessage: string = await firstValueFrom(this.translateService.get('main.errors.internal'));
    this.notifierService.notify('error', errorMessage);
    return MovementsActions.storeMovementFailure({ error: { message: message }, operation });
  }
}
