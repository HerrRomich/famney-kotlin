import { FixedSizeVirtualScrollStrategy } from '@angular/cdk/scrolling';
import { Injectable } from '@angular/core';

@Injectable()
export class AccountMovementsViertualScrollStrategy extends FixedSizeVirtualScrollStrategy {
  constructor() {
    super(40, 500, 1000);
  }
}
