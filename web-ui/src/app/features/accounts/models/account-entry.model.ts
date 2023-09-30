import { AccountMovement } from './account-movement.model';

export interface AccountEntry extends AccountMovement {
  entryItems: EntryItem[];
}

export interface EntryItem {
  categoryId: number;
  amount: number;
  comments?: string;
}
