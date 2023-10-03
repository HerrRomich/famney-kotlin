import { NgModule } from '@angular/core';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatLineModule, MatNativeDateModule, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LocaleService } from '@famoney-shared/services/locale.service';

export const MY_FORMATS = {
  parse: {
    dateInput: 'L',
  },
  display: {
    dateInput: 'L',
    monthYearLabel: 'MMM YYYY',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'MMMM YYYY',
  },
};

const MATERIAL_MODULES = [
  MatToolbarModule,
  MatCardModule,
  MatIconModule,
  MatNativeDateModule,
  MatBadgeModule,
  MatMenuModule,
  MatButtonModule,
  MatButtonToggleModule,
  MatTooltipModule,
  MatListModule,
  MatChipsModule,
  MatAutocompleteModule,
  MatInputModule,
  MatFormFieldModule,
  MatDatepickerModule,
  MatDialogModule,
  MatLineModule,
  MatSelectModule,
];

@NgModule({
  imports: [...MATERIAL_MODULES],
  exports: [...MATERIAL_MODULES],
  providers: [
    {
      provide: MAT_DATE_LOCALE,
      useFactory: (localeService: LocaleService) => {
        return localeService.locale;
      },
      deps: [LocaleService],
    },
    { provide: MAT_DATE_FORMATS, useValue: MY_FORMATS },
  ],
})
export class MaterialModule {}
