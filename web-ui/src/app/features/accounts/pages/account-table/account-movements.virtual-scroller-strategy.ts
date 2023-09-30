import { FixedSizeVirtualScrollStrategy } from '@angular/cdk/scrolling';
import { Injectable } from '@angular/core';

@Injectable()
export class AccountMovementsVirtualScrollStrategy extends FixedSizeVirtualScrollStrategy {
  constructor() {
    super(48, 500, 1000);
  }
}
