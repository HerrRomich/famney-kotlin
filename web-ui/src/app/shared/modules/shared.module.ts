import { HttpClient } from '@angular/common/http';
import { LOCALE_ID, NgModule } from '@angular/core';
import { LocaleService } from '@famoney-shared/services/locale.service';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { SpacerDirective } from '../directives/spacer.directive';
import { AngularModule } from './angular.module';

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
  ],
  exports: [TranslateModule, SpacerDirective],
  providers: [
    LocaleService,
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
}
