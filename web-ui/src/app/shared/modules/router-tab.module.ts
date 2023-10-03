import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { RouterModule } from '@angular/router';
import { RouterTabItemDirective } from '../components/router-tab/router-tab-item.directive';
import { RouterTabComponent } from '../components/router-tab/router-tab.component';
import { RouterTabDirective } from '../directives/router-tab.directive';

@NgModule({
  imports: [CommonModule, RouterModule, MatTabsModule],
  declarations: [RouterTabComponent, RouterTabItemDirective, RouterTabDirective],
  exports: [RouterTabComponent, RouterTabItemDirective, RouterTabDirective],
})
export class RouterTabModule {}
