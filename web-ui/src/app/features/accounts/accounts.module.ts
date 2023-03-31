import { NgModule } from '@angular/core';
import { AngularModule } from '@famoney-shared/modules/angular.module';
import { MaterialModule } from '@famoney-shared/modules/material.module';
import { SharedModule } from '@famoney-shared/modules/shared.module';
import { AccountsRoutingModule } from './accounts-routing.module';
import { RouterTabModule } from '../../shared/modules/router-tab.module';
import { AccountsComponent } from './pages/accounts/accounts.component';
import { AccountTableComponent } from './pages/account-table/account-table.component';
import { AccountEntryDialogComponent } from './components/account-entry-dialog';
import { MonthPickerModule } from '@famoney-shared/modules/month-picker.module';
import { AccountsStore } from './store/accounts.store';
import { MovementsStore } from './store/movements.store';
import { provideComponentStore } from '@ngrx/component-store';

@NgModule({
  declarations: [AccountsComponent, AccountTableComponent, AccountEntryDialogComponent],
  imports: [
    AngularModule,
    MaterialModule,
    SharedModule,
    AccountsRoutingModule,
    RouterTabModule,
    MonthPickerModule
  ],
  providers: [provideComponentStore(AccountsStore), provideComponentStore(MovementsStore)]
})
export class AccountsModule {}
