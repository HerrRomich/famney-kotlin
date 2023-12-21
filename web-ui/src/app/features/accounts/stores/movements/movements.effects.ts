import { inject, Injectable } from '@angular/core';
import { AccountsApiService, MovementDto } from '@famoney-apis/accounts';
import { MovementDialogService } from '@famoney-features/accounts/services/movement-dialog.service';
import { MovementsService } from '@famoney-features/accounts/services/movements.service';
import * as AccountsActions from '@famoney-features/accounts/stores/accounts/accounts.actions';
import * as AccountsSelectors from '@famoney-features/accounts/stores/accounts/accounts.selectors';
import * as MovementsActions from '@famoney-features/accounts/stores/movements/movements.actions';
import * as MovementsSelectors from '@famoney-features/accounts/stores/movements/movements.selectors';
import {
  MovementOperation,
  MovementsEntity,
  MovementsEntityEntry,
} from '@famoney-features/accounts/stores/movements/movements.state';
import { ConfirmationDialogService } from '@famoney-shared/services/confirmation-dialog.service';
import { EntryCategoryService } from '@famoney-shared/services/entry-category.service';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { TranslateService } from '@ngx-translate/core';
import { NotifierService } from 'angular-notifier';
import { multirange } from 'multi-integer-range';
import { concatMap, firstValueFrom, of, OperatorFunction, pipe, switchMap, withLatestFrom } from 'rxjs';
import { fromPromise } from 'rxjs/internal/observable/innerFrom';
import { catchError, combineLatestWith, map } from 'rxjs/operators';

@Injectable()
export class MovementsEffects {
  private readonly store = inject(Store);
  private readonly actions$ = inject(Actions);
  private readonly accountsApiService = inject(AccountsApiService);
  private readonly movementsService = inject(MovementsService);
  private readonly entryCategoriesService = inject(EntryCategoryService);
  private readonly movementDialogService = inject(MovementDialogService);
  private readonly confirmationDialogService = inject(ConfirmationDialogService);
  private notifierService = inject(NotifierService);
  private translateService = inject(TranslateService);

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
        return this.accountsApiService.readMovements(currentAccount.id, undefined, undefined, min, max - min + 1).pipe(
          this.mapMovementsToEntries(),
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
    return of(MovementsActions.loadMovementsRangeFailure({ error: { message } }));
  }

  private mapMovementsToEntries(): OperatorFunction<MovementDto[], MovementsEntityEntry[]> {
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

  private mapMovementToEntity(): OperatorFunction<MovementDto, MovementsEntity> {
    return pipe(
      combineLatestWith(this.entryCategoriesService.entryCategoriesForVisualisation$),
      map(([movement, entryCategories]) => {
        const categoryId = this.movementsService.getEntryItemData(movement)?.categoryId;
        const category = categoryId ? entryCategories.flatEntryCategories.get(categoryId) : undefined;
        return {
          pos: movement.position,
          entry: {
            movement,
            category,
          },
        };
      }),
    );
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
                  this.mapMovementToEntity(),
                  map((entity) =>
                    MovementsActions.storeMovementSuccess({
                      entity,
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
      concatMap(([{ pos, operation }, accountId, movementEntities]) => {
        const movementsEntity = movementEntities[pos];
        if (!accountId) {
          return fromPromise(this.getInternalFailure(operation, 'No account is selected!'));
        }
        const movement = movementsEntity?.entry?.movement;
        if (!movement?.data) {
          return fromPromise(this.getInternalFailure(operation, 'No movement is selected!'));
        }
        const updatedMovementData$ =
          movement.data.type === 'ENTRY' ? this.movementDialogService.editMovementEntry(movement?.data) : of(undefined);
        return updatedMovementData$.pipe(
          concatMap((updatedMovementData) =>
            updatedMovementData
              ? this.accountsApiService.updateMovement(accountId, movement.id, updatedMovementData).pipe(
                  this.mapMovementToEntity(),
                  map((entity) =>
                    MovementsActions.storeMovementSuccess({
                      pos,
                      entity,
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
      concatMap(([{ pos, operation }, accountId, movementEntities]) => {
        const movementsEntity = movementEntities[pos];
        if (!accountId) {
          return fromPromise(this.getInternalFailure(operation, 'No account is selected!'));
        }
        const movement = movementsEntity?.entry?.movement;
        if (!movement) {
          return fromPromise(this.getInternalFailure(operation, 'No movement is selected!'));
        }
        return this.confirmationDialogService.query('').pipe(
          concatMap((confirmed) =>
            confirmed
              ? this.accountsApiService.deleteMovement(accountId, movement.id).pipe(
                  map(() =>
                    MovementsActions.storeMovementSuccess({
                      pos,
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
