import { CdkVirtualScrollViewport, VIRTUAL_SCROLL_STRATEGY } from '@angular/cdk/scrolling';
import { AfterViewInit, ChangeDetectionStrategy, Component, Inject, OnDestroy, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { EcoFabSpeedDialActionsComponent, EcoFabSpeedDialComponent } from '@ecodev/fab-speed-dial';
import { MovementDto } from '@famoney-apis/accounts';
import { MovementEntryDialogComponent } from '@famoney-features/accounts/components/movement-entry-dialog';
import { EntryDialogData } from '@famoney-features/accounts/models/account-entry.model';
import { AccountsStore, MovementData } from '@famoney-features/accounts/store/accounts.store';
import { EntryCategoryService } from '@famoney-shared/services/entry-category.service';
import { TranslateService } from '@ngx-translate/core';
import { NotificationsService } from 'angular2-notifications';
import { EMPTY, interval, of, Subject } from 'rxjs';
import { debounce, map, takeUntil, tap } from 'rxjs/operators';
import { AccountMovementsViertualScrollStrategy } from './account-movements.virtual-scroller-strategy';
import { MovementDataSource } from './movement-data-source';

const fabSpeedDialDelayOnHover = 350;

@Component({
  selector: 'fm-account-table',
  templateUrl: 'account-table.component.html',
  styleUrls: ['account-table.component.scss'],
  providers: [
    {
      provide: VIRTUAL_SCROLL_STRATEGY,
      useClass: AccountMovementsViertualScrollStrategy,
    },
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountTableComponent implements AfterViewInit, OnDestroy {
  movementDataSource: MovementDataSource = new MovementDataSource(this._accountsStore);

  @ViewChild('fabSpeedDial', { static: true })
  fabSpeedDial!: EcoFabSpeedDialComponent;

  @ViewChild('fabSpeedDialActions', { static: true })
  fabSpeedDialActions!: EcoFabSpeedDialActionsComponent;

  @ViewChild(CdkVirtualScrollViewport)
  viewPort!: CdkVirtualScrollViewport;

  private _speedDialHovered$ = new Subject<boolean>();
  private _subscriptionDestroyed = new Subject<void>();
  movementSelection$ = new Subject<number | undefined>();

  constructor(
    private _accountsStore: AccountsStore,
    private _accountEntryDialogComponent: MatDialog,
    private _entryCategoriesService: EntryCategoryService,
    private _notificationsService: NotificationsService,
    private _translateService: TranslateService,
    @Inject(VIRTUAL_SCROLL_STRATEGY)
    private _accountMovementsViertualScrollStrategy: AccountMovementsViertualScrollStrategy,
  ) {}

  ngAfterViewInit() {
    this.fabSpeedDial.openChange
      .pipe(
        tap(opened => {
          this.fabSpeedDial.fixed = !opened;
        }),
        takeUntil(this._subscriptionDestroyed),
      )
      .subscribe();

    this._speedDialHovered$
      .pipe(
        debounce(hovered => hovered ? of(0) : interval(fabSpeedDialDelayOnHover)),
        tap(hovered => {
          if (hovered != this.fabSpeedDial.open) {
            this.fabSpeedDial.toggle();
          }
        }),
        takeUntil(this._subscriptionDestroyed),
      )
      .subscribe();
  }

  ngOnDestroy() {
    this._subscriptionDestroyed.next();
    this._subscriptionDestroyed.complete();
    this.movementSelection$.complete();
    this._speedDialHovered$.complete();
  }

  get inverseTranslation(): string {
    if (!this.viewPort || !this.viewPort['_renderedContentTransform']) {
      return '-0px';
    }
    return `-${this.viewPort['_renderedContentOffset']}px`;
  }

  trackByFn(index: number, item: MovementData) {
    return item.pos;
  }

  getSumColor(sum: number | undefined) {
    return !sum ? undefined : sum > 0 ? 'positive-amount' : 'negative-amount';
  }

  triggerSpeedDial() {
    this._speedDialHovered$.next(true);
  }

  stopSpeedDial() {
    this._speedDialHovered$.next(false);
  }

  getMovementComments(movement?: MovementDto) {
    const movementData = movement?.data;
    switch (movementData?.type) {
      case 'ENTRY':
        const entryItems = movementData?.entryItems ?? [];
        return entryItems.length === 1 ? entryItems[0].comments : undefined;
      case 'REFUND':
      case 'TRANSFER':
        return movementData?.comments;
      default:
        return undefined;
    }
  }

  getMovementCategoryPath$(movement?: MovementDto) {
    const movementData = movement?.data;
    switch (movementData?.type) {
      case 'ENTRY':
        const entryItems = movementData?.entryItems ?? [];
        return entryItems.length === 1 ? this.getCategoryPathById$(entryItems[0].categoryId) : EMPTY;
      case 'REFUND':
        return this.getCategoryPathById$(movementData?.categoryId);
      default:
        return EMPTY;
    }
  }

  private getCategoryPathById$(categoryId: number) {
    return this._entryCategoriesService.entryCategoriesForVisualisation$.pipe(
      map(entryCategories => entryCategories.flatEntryCategories.get(categoryId)?.fullPath),
    );
  }

  openMenu(movement: MovementDto) {
    this.movementSelection$.next(movement.id);
  }

  closeMenu() {
    this.movementSelection$.next(undefined);
  }

  addEntry() {
    console.log('Edit movement.');
    /*this.stopSpeedDial();
    if (this._accountDTO === undefined) {
      this.showNoAccountErrorNotification();
      return;
    }
    const accountId = this._accountDTO.id;
    this.openAccountEntryDialog({
      accountId: accountId,
    }).subscribe();*/
  }

  private showNoAccountErrorNotification() {
    this._translateService
      .get(['notifications.title.error', 'accounts.table.errors.noAccount'])
      .pipe(
        tap((errorMesages: { [key: string]: string }) =>
          this._notificationsService.error(
            errorMesages['notifications.title.error'],
            errorMesages['accounts.table.errors.noAccount'],
          ),
        ),
      )
      .subscribe();
  }

  private openAccountEntryDialog(data: EntryDialogData) {
    const accountEntryDialogRef = this._accountEntryDialogComponent.open<
      MovementEntryDialogComponent,
      EntryDialogData,
      MovementDto
    >(MovementEntryDialogComponent, {
      width: '520px',
      minWidth: '520px',
      maxWidth: '520px',
      panelClass: 'account-entry-dialog',
      disableClose: true,
      hasBackdrop: true,
      data: data,
    });
    return accountEntryDialogRef.afterClosed();
  }

  addTransfer() {
    console.log('Add transfer.');
  }

  addRefund() {
    console.log('Add refund.');
  }

  edit(movement: MovementDto) {
    console.log('Edit movement.');
    /*if (this._accountDTO === undefined) {
      this.showNoAccountErrorNotification();
      return;
    }
    const accountId = this._accountDTO.id;
    if (movement.data?.type === 'ENTRY') {
      this.openAccountEntryDialog({
        accountId: accountId,
        movementId: movement.id,
        entryData: movement.data,
      }).subscribe();
    }*/
  }
}
