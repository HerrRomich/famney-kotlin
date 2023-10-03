import {
  AfterViewInit,
  Component,
  ContentChildren,
  DestroyRef,
  inject,
  QueryList,
  ViewChild,
  ViewChildren,
  ViewEncapsulation,
} from '@angular/core';

import { MatTabGroup } from '@angular/material/tabs';

import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { IsActiveMatchOptions, NavigationEnd, Router } from '@angular/router';
import { RouterTabDirective } from '../../directives/router-tab.directive';
import { RouterTabItemDirective } from './router-tab-item.directive';

@Component({
  selector: 'fm-router-tab',
  templateUrl: './router-tab.component.html',
  styleUrls: ['./router-tab.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class RouterTabComponent implements AfterViewInit {
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  @ViewChild('matTabGroup', { static: true })
  public matTabGroup!: MatTabGroup;

  @ContentChildren(RouterTabItemDirective)
  public routerTabItems!: QueryList<RouterTabItemDirective>;

  @ViewChildren(RouterTabDirective)
  public routerTabs!: QueryList<RouterTabDirective>;

  ngAfterViewInit() {
    // Remove tab click event
    this.matTabGroup._handleClick = () => {};
    // Select current tab depending on url
    this.setIndex();
    // Subscription to navigation change
    this.router.events.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((e) => {
      if (e instanceof NavigationEnd) {
        this.setIndex();
      }
    });
  }

  /**
   * Set current selected tab depending on navigation
   */
  private setIndex() {
    this.routerTabs.find((tab, i) => {
      const matchOptions: IsActiveMatchOptions = tab.routerLinkActiveOptions?.exact
        ? { paths: 'exact', queryParams: 'exact', fragment: 'ignored', matrixParams: 'ignored' }
        : { paths: 'subset', queryParams: 'subset', fragment: 'ignored', matrixParams: 'ignored' };
      if (tab.routerLink.urlTree === null || !this.router.isActive(tab.routerLink.urlTree, matchOptions)) {
        return false;
      }
      tab.tab.isActive = true;
      this.matTabGroup.selectedIndex = i;
      return true;
    });
  }
}
