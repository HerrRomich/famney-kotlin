import { inject, Injectable } from '@angular/core';
import { AccountDto, AccountsApiService } from '@famoney-apis/accounts';
import { AccountsFilterStorageService } from '@famoney-features/accounts/services/accounts-filter-storage.service';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { NotifierService } from 'angular-notifier';
import { EMPTY, switchMap, withLatestFrom } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import * as AccountsActions from './accounts.actions';
import * as AccountsSeletors from './accounts.selectors';

@Injectable()
export class AccountsEffects {
  private readonly store = inject(Store);
  private readonly actions$ = inject(Actions);
  private readonly filterStorageService = inject(AccountsFilterStorageService);
  private readonly accountsApiService = inject(AccountsApiService);
  private readonly notifierService = inject(NotifierService);

  readonly init$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AccountsActions.initAccounts),
      map(() => AccountsActions.loadAccounts()),
    ),
  );

  readonly loadAccounts$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AccountsActions.loadAccounts),
      withLatestFrom(this.store.select(AccountsSeletors.accountsLoadedSelector)),
      switchMap(([, loaded]) => {
        if (loaded) {
          return EMPTY;
        }
        return this.accountsApiService.readAccounts().pipe(
          map((accounts) => {
            const currentSelectedTags = this.filterStorageService.restoreAccountsSelectedTags();
            const allTags = this.getAllTags(accounts);
            const selectedTags = currentSelectedTags.filter((tag) => allTags.includes(tag)).sort();
            return AccountsActions.loadAccountsSuccess({ accounts, selectedTags });
          }),
          catchError(() => {
            this.notifierService.notify('error', "Couldn't load list of accounts.");
            return EMPTY;
          }),
        );
      }),
    ),
  );

  readonly addTagToSelection$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AccountsActions.addTagToSelection),
      withLatestFrom(
        this.store.select(AccountsSeletors.allTagsSelector),
        this.store.select(AccountsSeletors.currentTagsSelector),
      ),
      map(([{ tag }, allTags, currentTags]) => {
        if (allTags.includes(tag)) {
          const selectedTags = [...currentTags, tag];
          this.filterStorageService.storeAccountsSelectedTags(selectedTags);
          return AccountsActions.changeTagsSuccess({
            selectedTags,
          });
        } else {
          return AccountsActions.changeTagsFailure({ error: { message: `Unknown tag: "${tag}"` } });
        }
      }),
    ),
  );

  readonly removeTagFromSelection$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AccountsActions.removeTagFromSelection),
      withLatestFrom(
        this.store.select(AccountsSeletors.allTagsSelector),
        this.store.select(AccountsSeletors.currentTagsSelector),
      ),
      map(([{ tag }, , currentTags]) => {
        if (currentTags.includes(tag)) {
          const selectedTags = currentTags.filter((currentTag) => currentTag !== tag);
          this.filterStorageService.storeAccountsSelectedTags(selectedTags);
          return AccountsActions.changeTagsSuccess({
            selectedTags,
          });
        } else {
          return AccountsActions.changeTagsFailure({ error: { message: `Not selected tag: "${tag}"` } });
        }
      }),
    ),
  );

  readonly clearSelectedTags$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AccountsActions.clearSelectedTags),
      map(() => {
        const selectedTags: string[] = [];
        this.filterStorageService.storeAccountsSelectedTags(selectedTags);
        return AccountsActions.changeTagsSuccess({
          selectedTags,
        });
      }),
    ),
  );

  readonly selectAccount$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AccountsActions.selectAccount),
      withLatestFrom(this.store.select(AccountsSeletors.allAccountEntitiesSelector)),
      map(([{ selectionId }, accounts]) => {
        if (accounts[selectionId]) {
          return AccountsActions.selectAccountSuccess({ selectionId });
        } else {
          return AccountsActions.selectAccountsFailure({
            error: {
              message: `Unknown account accountId: ${selectionId}.`,
            },
          });
        }
      }),
    ),
  );

  private getAllTags(accounts: AccountDto[]): string[] {
    return accounts.reduce(
      (tags, account) => (account.tags ? [...tags, ...account.tags.filter((tag) => !tags.includes(tag))] : tags),
      Array<string>(),
    );
  }
}
