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
import { EcoFabSpeedDialModule } from '@ecodev/fab-speed-dial';
import { OverlayModule } from '@angular/cdk/overlay';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { AccountTagsPopupComponent } from './components/account-tags-popup';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatButtonModule } from '@angular/material/button';

@NgModule({
  declarations: [AccountsComponent, AccountTableComponent, AccountEntryDialogComponent, AccountTagsPopupComponent],
  imports: [
    AngularModule,
    OverlayModule,
    EcoFabSpeedDialModule,
    MaterialModule,
    SharedModule,
    AccountsRoutingModule,
    RouterTabModule,
    MonthPickerModule,
  ],
  providers: [provideComponentStore(AccountsStore), provideComponentStore(MovementsStore)],
})
export class AccountsModule {}
