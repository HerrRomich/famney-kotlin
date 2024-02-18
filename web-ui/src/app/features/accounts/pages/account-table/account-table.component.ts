import { CdkVirtualScrollViewport, VIRTUAL_SCROLL_STRATEGY } from '@angular/cdk/scrolling';
import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnDestroy,
  ViewChild,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { EcoFabSpeedDialActionsComponent, EcoFabSpeedDialComponent } from '@ecodev/fab-speed-dial';
import { AccountsFacade } from '@famoney-features/accounts/stores/accounts/accounts.facade';
import { MovementsFacade } from '@famoney-features/accounts/stores/movements/movements.facade';

import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Subject, interval, of } from 'rxjs';
import { debounce, map } from 'rxjs/operators';
import { AccountMovementsVirtualScrollStrategy } from './account-movements.virtual-scroller-strategy';
import { MovementsDataSource, VisualMovement } from './movements-data-source.service';

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
    MovementsDataSource,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountTableComponent implements AfterViewInit, OnDestroy {
  private accountsFacade = inject(AccountsFacade);
  private movementsFacade = inject(MovementsFacade);
  private destroyRef = inject(DestroyRef);
  protected layoutBreakpoint = inject(BreakpointObserver).observe([Breakpoints.HandsetPortrait]);
  protected movementsDataSource = inject(MovementsDataSource);
  private virtualScrollerStrategy = inject(VIRTUAL_SCROLL_STRATEGY);
  protected layout$ = this.layoutBreakpoint.pipe(map((state) => (state.matches ? 'mobile' : 'web')));
  private scrolledIndex = toSignal(this.virtualScrollerStrategy.scrolledIndexChange);
  private speedDialHovered$ = new Subject<boolean>();
  protected movementSelection = signal<number | undefined>(undefined);

  constructor() {
    const virtualScrollerStrategy = this.virtualScrollerStrategy;
    if (virtualScrollerStrategy instanceof AccountMovementsVirtualScrollStrategy) {
      this.layout$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((layout) => {
        const scrolledIndex = this.scrolledIndex() ?? 0;
        if (layout === 'web') {
          virtualScrollerStrategy.updateItemAndBufferSize(49, 1000, 10000);
        } else {
          virtualScrollerStrategy.updateItemAndBufferSize(54, 1600, 16000);
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

  trackByFn(index: number, item?: VisualMovement): number | undefined {
    return item?.id;
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

  openMenu(pos: number) {
    this.movementSelection.set(pos);
  }

  closeMenu() {
    this.movementSelection.set(undefined);
  }

  async addEntry() {
    await this.movementsFacade.addMovementEntry('ENTRY');
  }

  async addTransfer() {
    await this.movementsFacade.addMovementEntry('TRANSFER');
  }

  async addRefund() {
    await this.movementsFacade.addMovementEntry('REFUND');
  }

  async edit(id: number) {
    await this.movementsFacade.editMovementEntry(id);
  }

  async delete(id: number) {
    await this.movementsFacade.deleteMovementEntry(id);
  }
}
