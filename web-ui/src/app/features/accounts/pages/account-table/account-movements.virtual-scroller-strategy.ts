import { CdkVirtualScrollViewport, FixedSizeVirtualScrollStrategy } from '@angular/cdk/scrolling';
import { Injectable } from '@angular/core';
import { MovementsStore } from '@famoney-features/accounts/store/movements.store';
import { interval, Subject, Subscription } from 'rxjs';
import { delay, switchMap, take, tap } from 'rxjs/operators';

@Injectable()
export class AccountMovementsViertualScrollStrategy extends FixedSizeVirtualScrollStrategy {
  constructor() {
    super(40, 200, 300);
  }
}
