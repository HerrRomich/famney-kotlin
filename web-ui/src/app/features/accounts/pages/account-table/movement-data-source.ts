import { CollectionViewer, DataSource } from '@angular/cdk/collections';
import { MovementData, MovementsStore as MovementsStore } from '@famoney-features/accounts/store/movements.store';
import { Subscription } from 'rxjs';
import { debounce, debounceTime, mergeMap, takeUntil, tap } from 'rxjs/operators';

export interface Movement {}

export class MovementDataSource extends DataSource<MovementData> {
  private subscription?: Subscription;
  constructor(private _movementsStore: MovementsStore) {
    super();
  }

  connect(collectionViewer: CollectionViewer) {
    this.subscription = collectionViewer.viewChange
      .pipe(
        debounceTime(50),
        tap(range => this._movementsStore.loadData([range.start, range.end])))
      .subscribe();
    return this._movementsStore.movements$;
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.subscription?.unsubscribe();
  }
}
