import { NgModule } from '@angular/core';
import { AngularModule } from '@famoney-shared/modules/angular.module';
import { MaterialModule } from '@famoney-shared/modules/material.module';
import { SharedModule } from '@famoney-shared/modules/shared.module';
import { AccountsRoutingModule } from './accounts-routing.module';
import { RouterTabModule } from './components/router-tab/router-tab.module';
import { AccountsComponent } from './pages/accounts/accounts.component';
import {ApiModule as AccountsApiModule, Configuration as AccountsApiConfiguration} from '@famoney-apis/accounts';

const accountsApiConfigFactory = () => {
  return new AccountsApiConfiguration({
    basePath: '/apis/accounts-api',
  });
};

@NgModule({
  declarations: [AccountsComponent],
  imports: [
    AngularModule,
    MaterialModule,
    SharedModule,
    AccountsRoutingModule,
    RouterTabModule,
    AccountsApiModule.forRoot(accountsApiConfigFactory)
  ],
})
export class AccountsModule {}
