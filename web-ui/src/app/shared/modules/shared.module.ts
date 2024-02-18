import { registerLocaleData } from '@angular/common';
import localeEn from '@angular/common/locales/en';
import localeRu from '@angular/common/locales/ru';
import { LOCALE_ID, NgModule } from '@angular/core';
import {
  EcoFabSpeedDialActionsComponent,
  EcoFabSpeedDialComponent,
  EcoFabSpeedDialTriggerComponent,
} from '@ecodev/fab-speed-dial';
import { FocusHighlightDirective } from '@famoney-shared/directives/focus-highlight.directive';
import { ConfirmationDialogService } from '@famoney-shared/services/confirmation-dialog.service';
import { LocaleService } from '@famoney-shared/services/locale.service';
import { EntryCategoriesEffects } from '@famoney-shared/stores/entry-categories/entry-categories.effects';
import { EntryCategoriesFacade } from '@famoney-shared/stores/entry-categories/entry-categories.facade';
import {
  ENTRY_CATEGORIES_FEATURE_KEY,
  entryCategoriesReducer,
} from '@famoney-shared/stores/entry-categories/entry-categories.reducer';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { TranslateModule } from '@ngx-translate/core';
import { NotifierModule } from 'angular-notifier';
import { SpacerDirective } from '../directives/spacer.directive';
import { AngularModule } from './angular.module';

@NgModule({
  declarations: [SpacerDirective, FocusHighlightDirective],
  imports: [
    AngularModule,
    NotifierModule,
    EcoFabSpeedDialComponent,
    EcoFabSpeedDialActionsComponent,
    EcoFabSpeedDialTriggerComponent,
    StoreModule.forFeature(ENTRY_CATEGORIES_FEATURE_KEY, entryCategoriesReducer),
    EffectsModule.forFeature([EntryCategoriesEffects]),
  ],
  exports: [
    TranslateModule,
    SpacerDirective,
    EcoFabSpeedDialComponent,
    EcoFabSpeedDialActionsComponent,
    EcoFabSpeedDialTriggerComponent,
    NotifierModule,
    FocusHighlightDirective,
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
    ConfirmationDialogService,
    EntryCategoriesFacade,
  ],
})
export class SharedModule {
  constructor() {
    registerLocaleData(localeEn);
    registerLocaleData(localeRu);
  }
}
