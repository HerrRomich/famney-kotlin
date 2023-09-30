import { inject, Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { AccountsApiService, EntryDataDto, MovementDto } from '@famoney-apis/accounts';
import { MovementEntryDialogComponent } from '@famoney-features/accounts/components/movement-entry-dialog';
import { MovementsService } from '@famoney-features/accounts/services/movements.service';
import * as AccountsActions from '@famoney-features/accounts/stores/accounts/accounts.actions';
import * as AccountsSelectors from '@famoney-features/accounts/stores/accounts/accounts.selectors';
import * as MovementsActions from '@famoney-features/accounts/stores/movements/movements.actions';
import * as MovementsSelectors from '@famoney-features/accounts/stores/movements/movements.selectors';
import { MovementsEntityEntry } from '@famoney-features/accounts/stores/movements/movements.state';
import { EntryCategoryService } from '@famoney-shared/services/entry-category.service';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { TranslateService } from '@ngx-translate/core';
import { NotifierService } from 'angular-notifier';
import { multirange } from 'multi-integer-range';
import { EMPTY, of, OperatorFunction, pipe, switchMap, withLatestFrom } from 'rxjs';
import { catchError, combineLatestWith, map } from 'rxjs/operators';

@Injectable()
export class MovementsEffects {
  private readonly store = inject(Store);
  private readonly actions$ = inject(Actions);
  private readonly accountsApiService = inject(AccountsApiService);
  private readonly movementsService = inject(MovementsService);
  private readonly entryCategoriesService = inject(EntryCategoryService);
  private readonly accountEntryDialogComponent = inject(MatDialog);
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

  readonly addMovement$ = createEffect(() =>
    this.actions$.pipe(
      ofType(MovementsActions.addMovementEntry, MovementsActions.editMovementEntry),
      withLatestFrom(this.store.select(AccountsSelectors.currentAccountSelector)),
      switchMap(([action, account]) => {
        if (!account) {
          this.showNoAccountErrorNotification();
          return EMPTY;
        }
        let movementId: number | undefined;
        let entryData: EntryDataDto | undefined;
        if (action.type === '[ Movements] Edit Movement Entry') {
          movementId = action.id;
          entryData = action.entryData;
        }
        return this.openAccountEntryDialog(entryData).pipe(
          switchMap((movementData) =>
            movementData
              ? of(MovementsActions.storeMovement({ accountId: account.id, movementId, movementData }))
              : EMPTY,
          ),
        );
      }),
    ),
  );

  private openAccountEntryDialog(data?: EntryDataDto) {
    const accountEntryDialogRef = this.accountEntryDialogComponent.open<
      MovementEntryDialogComponent,
      EntryDataDto,
      EntryDataDto
    >(MovementEntryDialogComponent, {
      width: 'min(100vw, max(60vw, 400px))',
      maxWidth: '100vw',
      panelClass: 'fm-account-entry-dialog',
      disableClose: true,
      hasBackdrop: true,
      data,
    });
    return accountEntryDialogRef.afterClosed();
  }

  private showNoAccountErrorNotification() {
    this.translateService
      .get(['notifications.title.error', 'accounts.table.errors.noAccount'])
      .pipe()
      .subscribe((errorMesages: { [key: string]: string }) =>
        this.notifierService.notify('error', errorMesages['accounts.table.errors.noAccount']),
      );
  }

  readonly storeMovement$ = createEffect(() =>
    this.actions$.pipe(
      ofType(MovementsActions.storeMovement),
      switchMap(({ accountId, movementId, movementData }) => {
        const storeOperator =
          typeof movementId === 'undefined'
            ? () => this.accountsApiService.addMovement(accountId, movementData)
            : () => this.accountsApiService.changeMovement(accountId, movementId, movementData);
        return storeOperator().pipe(
          map(() => MovementsActions.storeMovementSuccess()),
          catchError((error) =>
            of(
              MovementsActions.storeMovementFailure({
                error: {
                  message: error.message ?? 'Failure storing movement!',
                },
              }),
            ),
          ),
        );
      }),
    ),
  );
}
