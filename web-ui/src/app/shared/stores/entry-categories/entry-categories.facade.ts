import { inject, Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { defer } from 'rxjs';
import * as EntryCategoriesActions from './entry-categories.actions';
import * as EntryCategoriesSelectors from './entry-categories.selectors';

@Injectable()
export class EntryCategoriesFacade {
  private store = inject(Store);
  readonly entryCategories$ = defer(() => {
    this.loadMovementsRange();
    return this.store.select(EntryCategoriesSelectors.selectEntryCategories);
  });

  private loadMovementsRange() {
    this.store.dispatch(EntryCategoriesActions.loadEntryCategories());
  }
}
