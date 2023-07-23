import {registerLocaleData} from '@angular/common';
import {HttpClient} from '@angular/common/http';
import localeEn from '@angular/common/locales/en';
import localeRu from '@angular/common/locales/ru';
import {LOCALE_ID, NgModule} from '@angular/core';
import {LocaleService} from '@famoney-shared/services/locale.service';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import {SpacerDirective} from '../directives/spacer.directive';
import {AngularModule} from './angular.module';
import {
  EcoFabSpeedDialActionsComponent,
  EcoFabSpeedDialComponent,
  EcoFabSpeedDialTriggerComponent
} from "@ecodev/fab-speed-dial";
import {NotifierModule} from "angular-notifier";

@NgModule({
  declarations: [SpacerDirective],
  imports: [
    AngularModule,
    TranslateModule.forRoot({
      defaultLanguage: 'ru',
      loader: {
        provide: TranslateLoader,
        useFactory: (httpClient: HttpClient) => new TranslateHttpLoader(httpClient, 'assets/i18n/'),
        deps: [HttpClient],
      },
    }),
    NotifierModule,
    EcoFabSpeedDialComponent,
    EcoFabSpeedDialActionsComponent,
    EcoFabSpeedDialTriggerComponent,
  ],
  exports: [TranslateModule, SpacerDirective, EcoFabSpeedDialComponent, EcoFabSpeedDialActionsComponent, EcoFabSpeedDialTriggerComponent, NotifierModule],
  providers: [
    {
      provide: LocaleService,
      useFactory: () => new LocaleService('ru'),
    },
    {
      provide: LOCALE_ID,
      useFactory: (localeService: LocaleService) => {
        return localeService.locale;
      },
      deps: [LocaleService],
    },
  ],
})
export class SharedModule {
  constructor() {
    registerLocaleData(localeEn);
    registerLocaleData(localeRu);
  }
}
