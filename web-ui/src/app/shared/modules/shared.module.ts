import { registerLocaleData } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import localeEn from '@angular/common/locales/en';
import localeRu from '@angular/common/locales/ru';
import { LOCALE_ID, NgModule } from '@angular/core';
import {
  EcoFabSpeedDialActionsComponent,
  EcoFabSpeedDialComponent,
  EcoFabSpeedDialTriggerComponent,
} from '@ecodev/fab-speed-dial';
import { LocaleService } from '@famoney-shared/services/locale.service';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { NotifierModule } from 'angular-notifier';
import { SpacerDirective } from '../directives/spacer.directive';
import { AngularModule } from './angular.module';

@NgModule({
  declarations: [SpacerDirective],
  imports: [
    AngularModule,
    NotifierModule,
    EcoFabSpeedDialComponent,
    EcoFabSpeedDialActionsComponent,
    EcoFabSpeedDialTriggerComponent,
  ],
  exports: [
    TranslateModule,
    SpacerDirective,
    EcoFabSpeedDialComponent,
    EcoFabSpeedDialActionsComponent,
    EcoFabSpeedDialTriggerComponent,
    NotifierModule,
  ],
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
