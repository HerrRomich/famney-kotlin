import { CdkOverlayOrigin, Overlay } from '@angular/cdk/overlay';
import { ComponentPortal } from '@angular/cdk/portal';
import { ChangeDetectionStrategy, Component, ViewChild } from '@angular/core';
import { AccountTagsPopupComponent } from '@famoney-features/accounts/components/accounts-filter-popup';
import { AccountsStore } from '@famoney-features/accounts/store/accounts.store';
import { map } from 'rxjs';

@Component({
  selector: 'fm-accounts',
  templateUrl: 'accounts.component.html',
  styleUrls: ['accounts.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountsComponent {
  readonly accountTagsText$ = this.accountsStore.selectedTags$.pipe(
    map(selectedTags => selectedTags?.map(tag => '- ' + tag).join('\n')),
  );

  readonly accountTagsCount$ = this.accountsStore.selectedTags$.pipe(map(selectedTags => selectedTags?.length));

  @ViewChild('accountTagsPopupButton', { static: true }) accountTagsPopupButton!: CdkOverlayOrigin;

  private _accountTagsPopupPortal: ComponentPortal<AccountTagsPopupComponent>;

  constructor(public accountsStore: AccountsStore, private overlay: Overlay) {
    this._accountTagsPopupPortal = new ComponentPortal(AccountTagsPopupComponent);
  }

  openAccountTagsPopup() {
    if (!this.accountTagsPopupButton) {
      return;
    }
    const position = this.overlay
      .position()
      .flexibleConnectedTo(this.accountTagsPopupButton.elementRef)
      .withPositions([{ originX: 'start', originY: 'bottom', overlayX: 'start', overlayY: 'top' }])
      .withFlexibleDimensions(true)
      .withGrowAfterOpen(true);
    const accountTagsPopup = this.overlay.create({
      disposeOnNavigation: true,
      positionStrategy: position,
      hasBackdrop: true,
      panelClass: 'fm-tags-panel',
      backdropClass: 'cdk-overlay-dark-backdrop',
    });
    accountTagsPopup.attach(this._accountTagsPopupPortal);
    accountTagsPopup.backdropClick().subscribe(() => accountTagsPopup.detach());
  }
}
