import { inject, Injectable } from '@angular/core';
import { MovementDataDto } from '@famoney-apis/accounts';
import { MovementOperation } from '@famoney-features/accounts/stores/movements/movements.state';
import { select, Store } from '@ngrx/store';
import { Range } from 'multi-integer-range';
import { firstValueFrom } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { v4 as uuidV4 } from 'uuid';
import * as MovementsActions from './movements.actions';
import * as MovementsSelectors from './movements.selectors';

@Injectable()
export class MovementsFacade {
  private store = inject(Store);
  readonly movements$ = this.store.select(MovementsSelectors.selectAllMovements);
  private readonly operation$ = this.store.pipe(
    select(MovementsSelectors.selectOperation),
    filter((operation): operation is MovementOperation => !!operation),
  );

  loadMovementsRange(range: Range) {
    this.store.dispatch(MovementsActions.loadMovementsRange({ range }));
  }

  async addMovementEntry(movementType: MovementDataDto['type']): Promise<void> {
    const correlationId = uuidV4();
    const subscriptionPromise = this.subscribeToOperation(correlationId);
    this.store.dispatch(
      MovementsActions.createMovement({
        movementType,
        operation: {
          type: 'createMovement',
          correlationId,
        },
      }),
    );
    return subscriptionPromise;
  }

  async editMovementEntry(pos: number): Promise<void> {
    const correlationId = uuidV4();
    const subscriptionPromise = this.subscribeToOperation(correlationId);
    this.store.dispatch(
      MovementsActions.updateMovement({
        pos,
        operation: {
          type: 'updateMovement',
          correlationId,
        },
      }),
    );
    return subscriptionPromise;
  }

  async deleteMovementEntry(pos: number): Promise<void> {
    const correlationId = uuidV4();
    const subscriptionPromise = this.subscribeToOperation(correlationId);
    this.store.dispatch(
      MovementsActions.deleteMovement({
        pos,
        operation: {
          type: 'deleteMovement',
          correlationId,
        },
      }),
    );
    return await subscriptionPromise;
  }

  private async subscribeToOperation(correlationId: string): Promise<void> {
    return await firstValueFrom(
      this.operation$.pipe(
        filter((operation) => operation.correlationId === correlationId),
        map((operation) => {
          if (operation.error) {
            throw Error(operation.error);
          }
        }),
      ),
    );
  }
}
