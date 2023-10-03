import { FixedSizeVirtualScrollStrategy } from '@angular/cdk/scrolling';
import { Injectable } from '@angular/core';

@Injectable()
export class AccountMovementsVirtualScrollStrategy extends FixedSizeVirtualScrollStrategy {
  constructor() {
    super(49, 500, 1000);
  }
}
