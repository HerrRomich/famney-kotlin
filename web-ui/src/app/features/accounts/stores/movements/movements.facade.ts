import { inject, Injectable } from '@angular/core';
import { Store } from '@ngrx/store';
import { Range } from 'multi-integer-range';
import * as movementsActions from './movements.actions';
import * as movementsSelectors from './movements.selectors';

@Injectable()
export class MovementsFacade {
  private store = inject(Store);
  readonly movements$ = this.store.select(movementsSelectors.selectAllMovements);

  loadMovementsRange(range: Range) {
    this.store.dispatch(movementsActions.loadMovementsRange({ range }));
  }
}
