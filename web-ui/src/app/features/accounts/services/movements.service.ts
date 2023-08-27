import { Injectable } from '@angular/core';
import { EntryItemDataDto, MovementDataDto, MovementDto } from '@famoney-apis/accounts';

@Injectable()
export class MovementsService {
  getEntryItemData(movement: MovementDto): EntryItemDataDto | undefined {
    const movementData = movement.data;
    switch (movementData?.type) {
      case 'ENTRY':
        return this.singleEntryItem(movementData?.entryItems);
      case 'REFUND':
        return movementData;
      default:
        return undefined;
    }
  }

  getMovementComments(movementData?: MovementDataDto) {
    switch (movementData?.type) {
      case 'ENTRY':
        return this.singleEntryItem(movementData?.entryItems)?.comments;
      case 'REFUND':
      case 'TRANSFER':
        return movementData?.comments;
      default:
        return undefined;
    }
  }

  private singleEntryItem(entryItems: EntryItemDataDto[] | undefined) {
    return entryItems?.length === 1 ? entryItems[0] : undefined;
  }
}
