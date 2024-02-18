import { inject, Injectable } from '@angular/core';
import { MasterDataApiService } from '@famoney-apis/master-data';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { EMPTY, of, switchMap, withLatestFrom } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import * as EntryCategoriesActions from './entry-categories.actions';
import * as EntryCategoriesSelectors from './entry-categories.selectors';
import { Store } from '@ngrx/store';

@Injectable()
export class EntryCategoriesEffects {
  private readonly store = inject(Store);
  private readonly actions$ = inject(Actions);
  private readonly masterDataApiService = inject(MasterDataApiService);

  readonly loadEntryCategories$ = createEffect(() =>
    this.actions$.pipe(
      ofType(EntryCategoriesActions.loadEntryCategories),
      withLatestFrom(this.store.select(EntryCategoriesSelectors.selectEntryCategoriesState)),
      switchMap(([, { entryCategories }]) => {
        if (entryCategories) {
          return EMPTY;
        }
        return this.masterDataApiService.getEntryCategories().pipe(
          map((entryCategories) => EntryCategoriesActions.loadEntryCategoriesSuccess({ entryCategories })),
          catchError((error) => this.getLoadFailure(error.message ?? 'Loading data failed!')),
        );
      }),
    ),
  );

  private getLoadFailure(message: string) {
    return of(EntryCategoriesActions.loadEntryCategoriesFailure({ error: { message } }));
  }
}
