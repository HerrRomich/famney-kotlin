import { RouterTabComponent } from '../components/router-tab/router-tab.component';
import { RouterTabItemDirective } from '../components/router-tab/router-tab-item.directive';
import { RouterTabDirective } from '../directives/router-tab.directive';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';

@NgModule({
  imports: [CommonModule, RouterModule, MatTabsModule],
  declarations: [RouterTabComponent, RouterTabItemDirective, RouterTabDirective],
  exports: [RouterTabComponent, RouterTabItemDirective, RouterTabDirective],
})
export class RouterTabModule {}
