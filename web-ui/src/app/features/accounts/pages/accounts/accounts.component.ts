import { CdkOverlayOrigin, Overlay } from '@angular/cdk/overlay';
import { ComponentPortal } from '@angular/cdk/portal';
import { ChangeDetectionStrategy, Component, inject, ViewChild } from '@angular/core';
import { AccountTagsPopupComponent } from '@famoney-features/accounts/components/accounts-filter-popup';
import { AccountsFacade } from '@famoney-features/accounts/stores/accounts/accounts.facade';

@Component({
  selector: 'fm-accounts',
  templateUrl: 'accounts.component.html',
  styleUrls: ['accounts.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountsComponent {
  accountsFacade = inject(AccountsFacade);

  @ViewChild('accountTagsPopupButton')
  accountTagsPopupButton?: CdkOverlayOrigin;

  private readonly accountTagsPopupPortal: ComponentPortal<AccountTagsPopupComponent>;

  constructor(private overlay: Overlay) {
    this.accountTagsPopupPortal = new ComponentPortal(AccountTagsPopupComponent);
  }

  openAccountTagsPopup() {
    if (!this.accountTagsPopupButton) {
      return;
    }
    const position = this.overlay
      .position()
      .flexibleConnectedTo(this.accountTagsPopupButton.elementRef)
      .withPositions([
        {
          originX: 'start',
          originY: 'bottom',
          overlayX: 'start',
          overlayY: 'top',
        },
      ])
      .withFlexibleDimensions(true)
      .withGrowAfterOpen(true);
    const accountTagsPopup = this.overlay.create({
      disposeOnNavigation: true,
      positionStrategy: position,
      hasBackdrop: true,
      panelClass: 'fm-tags-panel',
      backdropClass: 'cdk-overlay-dark-backdrop',
    });
    accountTagsPopup.attach(this.accountTagsPopupPortal);
    accountTagsPopup.backdropClick().subscribe(() => accountTagsPopup.detach());
  }
}
