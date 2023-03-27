import { Injectable } from '@angular/core';
import { AccountsApiService, EntryDataDto } from '@famoney-apis/accounts';

@Injectable({
  providedIn: 'root',
})
export class MovementsService {
  constructor(
    private _accountsApiService: AccountsApiService,
  ) {}

  getMovements(accountId: number, offset: number, limit: number) {
    return this._accountsApiService
      .getMovements(accountId, offset, limit);
  }

  addMovement(accountId: number, entryData: EntryDataDto) {
    return this._accountsApiService
      .addMovement(accountId, entryData);
  }
  changeMovement(accountId: number, movementId: number, entryData: EntryDataDto) {
    return this._accountsApiService
      .changeMovement(accountId, movementId, entryData);
  }
}
