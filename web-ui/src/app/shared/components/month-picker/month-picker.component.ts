import { Component, OnInit } from '@angular/core';
import { MatDateFormats, MAT_DATE_FORMATS } from '@angular/material/core';
import { MatDatepicker, MAT_SINGLE_DATE_SELECTION_MODEL_PROVIDER } from '@angular/material/datepicker';
import { MonthCalendarHeaderComponent } from './month-calendar-header.component';

export const MY_FORMATS: MatDateFormats = {
  parse: {
    dateInput: null,
  },
  display: {
    dateInput: { year: 'numeric', month: 'short' },
    monthYearLabel: { year: 'numeric', month: 'short' },
    dateA11yLabel: { year: 'numeric', month: 'long', day: 'numeric' },
    monthYearA11yLabel: { year: 'numeric', month: 'long' },
  },
};

@Component({
  selector: 'fm-monthpicker',
  template: '',
  providers: [MAT_SINGLE_DATE_SELECTION_MODEL_PROVIDER, { provide: MAT_DATE_FORMATS, useValue: MY_FORMATS }],
})
export class MonthPickerComponent<D> extends MatDatepicker<D> implements OnInit {
  ngOnInit() {
    this.startView = 'year';
    this.calendarHeaderComponent = MonthCalendarHeaderComponent;
  }

  override _selectMonth(normalizedMonth: D) {
    super._selectMonth(normalizedMonth);
    this.select(normalizedMonth);
    this.close();
  }
}
