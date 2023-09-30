import { inject, Injectable } from '@angular/core';
import { MovementDto } from '@famoney-apis/accounts';
import { Store } from '@ngrx/store';
import { Range } from 'multi-integer-range';
import * as MovementsActions from './movements.actions';
import * as MovementsSelectors from './movements.selectors';

@Injectable()
export class MovementsFacade {
  private store = inject(Store);
  readonly movements$ = this.store.select(MovementsSelectors.selectAllMovements);

  loadMovementsRange(range: Range) {
    this.store.dispatch(MovementsActions.loadMovementsRange({ range }));
  }

  addMovementEntry() {
    this.store.dispatch(MovementsActions.addMovementEntry());
  }

  editMovementEntry(movement: MovementDto) {
    if (movement.data?.type === 'ENTRY') {
      this.store.dispatch(
        MovementsActions.editMovementEntry({
          id: movement.id,
          entryData: movement.data,
        }),
      );
    }
  }
}
