import { CollectionViewer, DataSource } from '@angular/cdk/collections';
import { AccountsStore, MovementData } from '@famoney-features/accounts/store/accounts.store';
import { Subscription } from 'rxjs';
import { debounceTime, tap } from 'rxjs/operators';

export interface Movement {}

export class MovementDataSource extends DataSource<MovementData> {
  private subscription?: Subscription;
  constructor(private _accountsStore: AccountsStore) {
    super();
  }

  connect(collectionViewer: CollectionViewer) {
    this.subscription = collectionViewer.viewChange
      .pipe(
        debounceTime(150),
        tap(range => this._accountsStore.loadData([range.start, range.end])))
      .subscribe();
    return this._accountsStore.movements$.pipe(
      tap(movements => console.log())
    );
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this.subscription?.unsubscribe();
  }
}
