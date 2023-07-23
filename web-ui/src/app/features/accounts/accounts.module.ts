import { OverlayModule } from '@angular/cdk/overlay';
import { NgModule } from '@angular/core';
import { AngularModule } from '@famoney-shared/modules/angular.module';
import { MaterialModule } from '@famoney-shared/modules/material.module';
import { MonthPickerModule } from '@famoney-shared/modules/month-picker.module';
import { SharedModule } from '@famoney-shared/modules/shared.module';
import { provideComponentStore } from '@ngrx/component-store';
import { RouterTabModule } from '../../shared/modules/router-tab.module';
import { AccountsRoutingModule } from './accounts-routing.module';
import { AccountDialogComponent } from './components/account-dialog';
import { AccountTagsPopupComponent } from './components/accounts-filter-popup';
import { MovementEntryDialogComponent } from './components/movement-entry-dialog';
import { AccountTableComponent } from './pages/account-table/account-table.component';
import { AccountsComponent } from './pages/accounts/accounts.component';
import { AccountsStore } from './store/accounts.store';

@NgModule({
  declarations: [
    AccountsComponent,
    AccountTableComponent,
    MovementEntryDialogComponent,
    AccountTagsPopupComponent,
    AccountDialogComponent,
  ],
    imports: [
        AngularModule,
        OverlayModule,
        MaterialModule,
        SharedModule,
        AccountsRoutingModule,
        RouterTabModule,
        MonthPickerModule,
    ],
  providers: [provideComponentStore(AccountsStore)],
})
export class AccountsModule {}
