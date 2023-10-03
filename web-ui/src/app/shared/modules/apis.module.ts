import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { ApiModule as AccountsApiModule, Configuration as AccountsApiConfiguration } from '@famoney-apis/accounts';
import {
  ApiModule as MasterDataApiModule,
  Configuration as MasterDataApiConfiguration,
} from '@famoney-apis/master-data';
import { DateHttpInterceptor, DATE_ATTRIBUTE_PATHS } from '@famoney-shared/http-interceptors/date.http-Interceptor';

const accountsApiConfigFactory = () => {
  return new AccountsApiConfiguration({
    basePath: '/apis/accounts-api',
  });
};

const masterDataApiConfigFactory = () => {
  return new MasterDataApiConfiguration({
    basePath: '/apis/master-data-api',
  });
};

@NgModule({
  imports: [
    AccountsApiModule.forRoot(accountsApiConfigFactory),
    MasterDataApiModule.forRoot(masterDataApiConfigFactory),
    HttpClientModule,
  ],
  providers: [
    {
      provide: DATE_ATTRIBUTE_PATHS,
      useValue: ['openDate', 'data->date', 'data->bookingDate', , 'data->budgetPeriod'],
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: DateHttpInterceptor,
      multi: true,
      deps: [DATE_ATTRIBUTE_PATHS],
    },
  ],
})
export class ApisModule {}
