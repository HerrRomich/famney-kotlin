import { CdkVirtualScrollViewport, VIRTUAL_SCROLL_STRATEGY } from '@angular/cdk/scrolling';
import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  effect,
  inject,
  OnDestroy,
  signal,
  ViewChild,
} from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { EcoFabSpeedDialActionsComponent, EcoFabSpeedDialComponent } from '@ecodev/fab-speed-dial';
import { MovementDto } from '@famoney-apis/accounts';
import { MovementsService } from '@famoney-features/accounts/services/movements.service';
import { AccountsFacade } from '@famoney-features/accounts/stores/accounts/accounts.facade';
import { MovementsFacade } from '@famoney-features/accounts/stores/movements/movements.facade';

import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { MovementsEntity } from '@famoney-features/accounts/stores/movements/movements.state';
import { interval, of, Subject } from 'rxjs';
import { debounce, map } from 'rxjs/operators';
import { AccountMovementsVirtualScrollStrategy } from './account-movements.virtual-scroller-strategy';
import { MovementDataSource } from './movement-data-source';

const fabSpeedDialDelayOnHover = 350;

@Component({
  selector: 'fm-account-table',
  templateUrl: 'account-table.component.html',
  styleUrls: ['account-table.component.scss'],
  providers: [
    {
      provide: VIRTUAL_SCROLL_STRATEGY,
      useClass: AccountMovementsVirtualScrollStrategy,
    },
    MovementsService,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountTableComponent implements AfterViewInit, OnDestroy {
  accountTableService = inject(MovementsService);
  private accountsFacade = inject(AccountsFacade);
  private movementsFacade = inject(MovementsFacade);
  private destroyRef = inject(DestroyRef);
  layoutBreakpoint = inject(BreakpointObserver).observe([Breakpoints.HandsetPortrait]);
  movementDataSource = new MovementDataSource(this.movementsFacade);
  private virtualScrollerStrategy = inject(VIRTUAL_SCROLL_STRATEGY);
  layout = toSignal(this.layoutBreakpoint.pipe(map((state) => (state.matches ? 'mobile' : 'web'))));
  scrolledIndex = toSignal(this.virtualScrollerStrategy.scrolledIndexChange);

  constructor() {
    const virtualScrollerStrategy = this.virtualScrollerStrategy;
    if (virtualScrollerStrategy instanceof AccountMovementsVirtualScrollStrategy) {
      effect(() => {
        const scrolledIndex = this.scrolledIndex() ?? 0;
        if (this.layout() === 'web') {
          virtualScrollerStrategy.updateItemAndBufferSize(49, 500, 1000);
        } else {
          virtualScrollerStrategy.updateItemAndBufferSize(75, 800, 1600);
        }
        virtualScrollerStrategy.scrollToIndex(scrolledIndex, 'auto');
      });
    }
  }

  @ViewChild('fabSpeedDial', { static: true })
  fabSpeedDial?: EcoFabSpeedDialComponent;

  @ViewChild('fabSpeedDialActions', { static: true })
  fabSpeedDialActions?: EcoFabSpeedDialActionsComponent;

  @ViewChild(CdkVirtualScrollViewport)
  viewPort?: CdkVirtualScrollViewport;

  private speedDialHovered$ = new Subject<boolean>();
  movementSelection = signal<number | undefined>(undefined);

  ngAfterViewInit() {
    this.fabSpeedDial?.openChange.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((opened) => {
      if (this.fabSpeedDial) {
        this.fabSpeedDial.fixed = !opened;
      }
    });

    this.speedDialHovered$
      .pipe(
        debounce((hovered) => (hovered ? of(0) : interval(fabSpeedDialDelayOnHover))),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((hovered) => {
        if (this.fabSpeedDial && hovered != this.fabSpeedDial.open) {
          this.fabSpeedDial.toggle();
        }
      });
  }

  ngOnDestroy() {
    this.speedDialHovered$.complete();
  }

  get inverseTranslation(): string {
    if (!this.viewPort || !this.viewPort['_renderedContentTransform']) {
      return '-0px';
    }
    return `-${this.viewPort['_renderedContentOffset']}px`;
  }

  trackByFn(index: number, item: MovementsEntity) {
    return item.pos;
  }

  getSumColorClass(sum: number | undefined) {
    return !sum ? undefined : sum > 0 ? 'positive-amount' : 'negative-amount';
  }

  triggerSpeedDial() {
    this.speedDialHovered$.next(true);
  }

  stopSpeedDial() {
    this.speedDialHovered$.next(false);
  }

  openMenu(movement: MovementDto) {
    this.movementSelection.set(movement.id);
  }

  closeMenu() {
    this.movementSelection.set(undefined);
  }

  addEntry() {
    this.movementsFacade.addMovementEntry();
  }

  addTransfer() {
    console.log('Add transfer.');
  }

  addRefund() {
    console.log('Add refund.');
  }

  edit(movement: MovementDto) {
    this.movementsFacade.editMovementEntry(movement);
  }
}
