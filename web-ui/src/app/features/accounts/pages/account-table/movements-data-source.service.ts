import { CollectionViewer, DataSource } from '@angular/cdk/collections';
import { Injectable, inject } from '@angular/core';
import { EntryDataDto, EntryItemDataDto, MovementDataDto, MovementDto, TransferDataDto } from '@famoney-apis/accounts';
import { AccountsFacade } from '@famoney-features/accounts/stores/accounts/accounts.facade';
import { MovementsFacade } from '@famoney-features/accounts/stores/movements/movements.facade';
import { EntryCategoriesFacade } from '@famoney-shared/stores/entry-categories/entry-categories.facade';
import { FlatEntryCategories } from '@famoney-shared/stores/entry-categories/entry-categories.state';
import { TranslateService } from '@ngx-translate/core';
import { Range } from 'multi-integer-range';
import { Observable, Subscription, combineLatest, concatMap, firstValueFrom, of } from 'rxjs';
import { fromPromise } from 'rxjs/internal/observable/innerFrom';
import { debounceTime } from 'rxjs/operators';

export type VisualMovement = {
  id: number;
  date: Date;
  category: string;
  comments?: string;
  amount: number;
  amountClass: string;
  total: number;
  totalClass: string;
};

@Injectable()
export class MovementsDataSource extends DataSource<VisualMovement | undefined> {
  private accountsFacade = inject(AccountsFacade);
  private movementsFacade = inject(MovementsFacade);
  private translateService = inject(TranslateService);
  private entryCategoriesFacade = inject(EntryCategoriesFacade);
  private subscription?: Subscription;

  connect(collectionViewer: CollectionViewer): Observable<readonly (VisualMovement | undefined)[]> {
    this.subscription = collectionViewer.viewChange
      .pipe(debounceTime(150))
      .subscribe((range) => this.movementsFacade.loadMovementsRange([range.start, range.end]));
    return combineLatest([this.movementsFacade.movements$, this.entryCategoriesFacade.entryCategories$]).pipe(
      concatMap(([{ movements, movementsRange, count }, { flatEntryCategories }]) => {
        const visualMovements = Array.from({ length: count }, (): VisualMovement | undefined => undefined);
        if (!movementsRange) {
          return of(visualMovements);
        }
        return fromPromise(this.getVisualMovements(visualMovements, movementsRange, count, movements, flatEntryCategories));
      }),
    );
  }

  disconnect(_collectionViewer: CollectionViewer): void {
    this.subscription?.unsubscribe();
  }

  private async getVisualMovements(
    visualMovements: (VisualMovement | undefined)[],
    movementsRange: Range,
    count: number,
    movements: MovementDto[],
    flatEntryCategories: FlatEntryCategories,
  ): Promise<(VisualMovement | undefined)[]> {
    const [min, max] = movementsRange;
    for (let pos = min; pos < max; pos++) {
      const movement = movements[pos - min];
      const category = await this.getMovementCategory(movement.data, flatEntryCategories);
      const comments = this.getMovementComments(movement.data);
      const {
        id,
        data: { date, amount },
        total,
      } = movement;
      visualMovements[pos] = {
        id,
        date,
        category,
        comments,
        amount,
        amountClass: this.getSumColorClass(amount),
        total,
        totalClass: this.getSumColorClass(total),
      };
    }
    return visualMovements;
  }

  private getMovementComments(movementData: MovementDataDto): string | undefined {
    switch (movementData.type) {
      case 'ENTRY':
        return this.singleEntryItem(movementData.entryItems)?.comments;
      case 'REFUND':
      case 'TRANSFER':
        return movementData.comments;
    }
  }

  private async getMovementCategory(
    movementData: MovementDataDto,
    flatEntryCategories: FlatEntryCategories,
  ): Promise<string> {
    switch (movementData.type) {
      case 'ENTRY':
        return await this.getEntryCategory(movementData, flatEntryCategories);
      case 'REFUND':
        return await firstValueFrom<string>(this.translateService.get('accounts.table.columns.category.values.refund'));
      case 'TRANSFER':
        return await this.getTransferCategory(movementData);
    }
  }

  private async getEntryCategory(movementData: EntryDataDto, flatEntryCategories: FlatEntryCategories) {
    const singleEntryItem = this.singleEntryItem(movementData.entryItems);
    return singleEntryItem
      ? flatEntryCategories.get(singleEntryItem.categoryId)?.fullPath ??
          (await firstValueFrom<string>(this.translateService.get('accounts.table.columns.category.values.unknown')))
      : await firstValueFrom<string>(
          this.translateService.get('accounts.table.columns.category.values.multiple-entries'),
        );
  }

  private async getTransferCategory(movementData: TransferDataDto) {
    const accounts = await firstValueFrom(this.accountsFacade.allAccountEntities$);
    const accountName = accounts[movementData.oppositAccountId]?.name;
    const directionKey =
      this.getSign(movementData.amount) === -1
        ? 'accounts.transfer.directions.to-account'
        : 'accounts.transfer.directions.from-account';
    const direction = await firstValueFrom<string>(this.translateService.get(directionKey));
    return await firstValueFrom<string>(
      this.translateService.get('accounts.table.columns.category.values.transfer', { direction, accountName }),
    );
  }

  private singleEntryItem(entryItems: EntryItemDataDto[] | undefined) {
    return entryItems?.length === 1 ? entryItems[0] : undefined;
  }

  private getSign(sum: number): -1 | 1 {
    return sum < 0 ? -1 : 1;
  }

  private getSumColorClass(sum: number): 'negative-amount' | 'positive-amount' {
    return this.getSign(sum) === -1 ? 'negative-amount' : 'positive-amount';
  }
}
