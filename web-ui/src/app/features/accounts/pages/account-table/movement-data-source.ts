import { CollectionViewer, DataSource } from '@angular/cdk/collections';
import { MovementsFacade } from '@famoney-features/accounts/stores/movements/movements.facade';
import { MovementsEntity } from '@famoney-features/accounts/stores/movements/movements.state';
import { Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

export class MovementDataSource extends DataSource<MovementsEntity> {
  private subscription?: Subscription;

  constructor(private movementsFacade: MovementsFacade) {
    super();
  }

  connect(collectionViewer: CollectionViewer) {
    this.subscription = collectionViewer.viewChange
      .pipe(debounceTime(150))
      .subscribe((range) => this.movementsFacade.loadMovementsRange([range.start, range.end]));
    return this.movementsFacade.movements$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.subscription?.unsubscribe();
  }
}
