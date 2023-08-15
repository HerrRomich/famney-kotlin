import { CdkVirtualScrollViewport, VIRTUAL_SCROLL_STRATEGY } from '@angular/cdk/scrolling';
import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  OnDestroy,
  signal,
  ViewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';
import { EcoFabSpeedDialActionsComponent, EcoFabSpeedDialComponent } from '@ecodev/fab-speed-dial';
import { EntryItemDataDto, MovementDataDto, MovementDto } from '@famoney-apis/accounts';
import { MovementEntryDialogComponent } from '@famoney-features/accounts/components/movement-entry-dialog';
import { EntryDialogData } from '@famoney-features/accounts/models/account-entry.model';
import { AccountsFacade } from '@famoney-features/accounts/stores/accounts/accounts.facade';
import { MovementsFacade } from '@famoney-features/accounts/stores/movements/movements.facade';
import { MovementsEntity } from '@famoney-features/accounts/stores/movements/movements.state';
import { EntryCategoryService } from '@famoney-shared/services/entry-category.service';
import { TranslateService } from '@ngx-translate/core';
import { NotifierService } from 'angular-notifier';
import { EMPTY, interval, of, Subject, switchMap, withLatestFrom } from 'rxjs';
import { debounce, map } from 'rxjs/operators';
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
  private accountEntryDialogComponent = inject(MatDialog);
  private entryCategoriesService = inject(EntryCategoryService);
  private notifierService = inject(NotifierService);
  private translateService = inject(TranslateService);
  private accountsFacade = inject(AccountsFacade);
  private movementsFacade = inject(MovementsFacade);
  private destroyRef = inject(DestroyRef);
  movementDataSource = new MovementDataSource(this.movementsFacade);

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

  getSumColor(sum: number | undefined) {
    return !sum ? undefined : sum > 0 ? 'positive-amount' : 'negative-amount';
  }

  triggerSpeedDial() {
    this.speedDialHovered$.next(true);
  }

  stopSpeedDial() {
    this.speedDialHovered$.next(false);
  }

  private getEntryItemData(movement?: MovementDto): EntryItemDataDto | undefined {
    const movementData = movement?.data;
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

  getMovementCategory$(movement?: MovementDto) {
    const categoryId = this.getEntryItemData(movement)?.categoryId;
    return categoryId
      ? this.entryCategoriesService.entryCategoriesForVisualisation$.pipe(
          map((entryCategories) => entryCategories.flatEntryCategories.get(categoryId)),
        )
      : EMPTY;
  }

  openMenu(movement: MovementDto) {
    this.movementSelection.set(movement.id);
  }

  closeMenu() {
    this.movementSelection.set(undefined);
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
    this.translateService
      .get(['notifications.title.error', 'accounts.table.errors.noAccount'])
      .pipe()
      .subscribe((errorMesages: { [key: string]: string }) =>
        this.notifierService.notify('error', errorMesages['accounts.table.errors.noAccount']),
      );
  }

  private openAccountEntryDialog(data: EntryDialogData) {
    const accountEntryDialogRef = this.accountEntryDialogComponent.open<
      MovementEntryDialogComponent,
      EntryDialogData,
      MovementDto
    >(MovementEntryDialogComponent, {
      width: '600px',
      minWidth: '600px',
      maxWidth: '600px',
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
    of(movement)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        withLatestFrom(this.accountsFacade.currentAccountId$),
        switchMap(([movement, accountId]) => {
          if (accountId && movement.data?.type === 'ENTRY') {
            return this.openAccountEntryDialog({
              accountId: accountId,
              movementId: movement.id,
              entryData: movement.data,
            });
          } else {
            this.showNoAccountErrorNotification();
            return EMPTY;
          }
        }),
      )
      .subscribe();
  }
}
