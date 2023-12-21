import { OverlayModule } from '@angular/cdk/overlay';
import { inject, NgModule } from '@angular/core';
import { EntryItemComponent, EntryItemService } from '@famoney-features/accounts/components/entry-item';
import { AccountTableComponent } from '@famoney-features/accounts/pages/account-table/account-table.component';
import { AccountsFilterStorageService } from '@famoney-features/accounts/services/accounts-filter-storage.service';
import { MovementDialogService } from '@famoney-features/accounts/services/movement-dialog.service';
import { MovementsService } from '@famoney-features/accounts/services/movements.service';
import { AccountsEffects } from '@famoney-features/accounts/stores/accounts/accounts.effects';
import { AccountsFacade } from '@famoney-features/accounts/stores/accounts/accounts.facade';
import { ACCOUNTS_FEATURE_KEY, accountsReducer } from '@famoney-features/accounts/stores/accounts/accounts.reducer';
import { MovementsEffects } from '@famoney-features/accounts/stores/movements/movements.effects';
import { MovementsFacade } from '@famoney-features/accounts/stores/movements/movements.facade';
import { MOVEMENTS_FEATURE_KEY, movementsReducer } from '@famoney-features/accounts/stores/movements/movements.reducer';
import { AngularModule } from '@famoney-shared/modules/angular.module';
import { MaterialModule } from '@famoney-shared/modules/material.module';
import { MonthPickerModule } from '@famoney-shared/modules/month-picker.module';
import { SharedModule } from '@famoney-shared/modules/shared.module';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { RouterTabModule } from '../../shared/modules/router-tab.module';
import { AccountsRoutingModule } from './accounts-routing.module';
import { AccountDialogComponent } from './components/account-dialog';
import { AccountTagsPopupComponent } from './components/accounts-filter-popup';
import { MovementEntryDialogComponent } from './components/movement-entry-dialog';
import { AccountsComponent } from './pages/accounts/accounts.component';

@NgModule({
  declarations: [
    AccountsComponent,
    AccountTableComponent,
    MovementEntryDialogComponent,
    AccountTagsPopupComponent,
    AccountDialogComponent,
    EntryItemComponent,
  ],
  imports: [
    AngularModule,
    OverlayModule,
    MaterialModule,
    SharedModule,
    AccountsRoutingModule,
    RouterTabModule,
    MonthPickerModule,
    StoreModule.forFeature(ACCOUNTS_FEATURE_KEY, accountsReducer),
    EffectsModule.forFeature([AccountsEffects]),
    StoreModule.forFeature(MOVEMENTS_FEATURE_KEY, movementsReducer),
    EffectsModule.forFeature([MovementsEffects]),
  ],
  providers: [
    EntryItemService,
    MovementsService,
    AccountsFilterStorageService,
    AccountsFacade,
    MovementsFacade,
    MovementDialogService,
  ],
})
export class AccountsModule {
  constructor() {
    const accountsFacade = inject(AccountsFacade);
    accountsFacade.init();
  }
}
