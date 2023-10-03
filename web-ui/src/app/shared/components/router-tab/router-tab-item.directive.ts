import { Directive, Input } from '@angular/core';

@Directive({ selector: 'fm-router-tab-item' })
export class RouterTabItemDirective {
  @Input()
  public routerLink?: string;

  @Input()
  public routerLinkActiveOptions?: {
    exact: boolean;
  };

  @Input()
  public disabled?: boolean;

  @Input()
  public label?: string;
}
