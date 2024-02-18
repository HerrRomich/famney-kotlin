import { inject, Injectable } from '@angular/core';
import { MovementDataDto } from '@famoney-apis/accounts/model/movement-data.dto';
import { select, Store } from '@ngrx/store';
import { Range } from 'multi-integer-range';
import { firstValueFrom } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { v4 as uuidV4 } from 'uuid';
import * as MovementsActions from './movements.actions';
import * as MovementsSelectors from './movements.selectors';
import { MovementOperation } from './movements.state';

@Injectable()
export class MovementsFacade {
  private store = inject(Store);
  readonly movements$ = this.store.select(MovementsSelectors.selectMovements);
  private readonly operation$ = this.store.pipe(
    select(MovementsSelectors.selectOperation),
    filter((operation): operation is MovementOperation => !!operation),
  );

  loadMovementsRange(range: Range) {
    this.store.dispatch(MovementsActions.loadMovementsRange({ movementsRange: range }));
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

  async editMovementEntry(id: number): Promise<void> {
    const correlationId = uuidV4();
    const subscriptionPromise = this.subscribeToOperation(correlationId);
    this.store.dispatch(
      MovementsActions.updateMovement({
        id,
        operation: {
          type: 'updateMovement',
          correlationId,
        },
      }),
    );
    return subscriptionPromise;
  }

  async deleteMovementEntry(id: number): Promise<void> {
    const correlationId = uuidV4();
    const subscriptionPromise = this.subscribeToOperation(correlationId);
    this.store.dispatch(
      MovementsActions.deleteMovement({
        id,
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
