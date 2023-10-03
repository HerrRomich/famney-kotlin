import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { CdkOverlayOrigin, Overlay } from '@angular/cdk/overlay';
import { ComponentPortal } from '@angular/cdk/portal';
import { ChangeDetectionStrategy, Component, inject, ViewChild } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { AccountTagsPopupComponent } from '@famoney-features/accounts/components/accounts-filter-popup';
import { AccountsFacade } from '@famoney-features/accounts/stores/accounts/accounts.facade';
import { map } from 'rxjs/operators';
import { MatSelectChange } from '@angular/material/select';

@Component({
  selector: 'fm-accounts',
  templateUrl: 'accounts.component.html',
  styleUrls: ['accounts.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountsComponent {
  private accountsFacade = inject(AccountsFacade);
  layoutBreakpoint = inject(BreakpointObserver).observe([Breakpoints.HandsetPortrait]);
  layout = toSignal(this.layoutBreakpoint.pipe(map((state) => (state.matches ? 'mobile' : 'web'))));

  protected filteredAccounts = toSignal(this.accountsFacade.filteredAccounts$);
  protected tagsCount = toSignal(this.accountsFacade.tagsCount$);
  protected tagsTexts = toSignal(this.accountsFacade.tagsTexts$);
  protected currentAccountId = toSignal(this.accountsFacade.currentAccountId$);

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

  selectAccount(event: MatSelectChange) {
    this.accountsFacade.selectAccount(event.value);
  }
}
