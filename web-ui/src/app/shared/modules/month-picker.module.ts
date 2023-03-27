import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MonthPickerComponent } from '../components/month-picker/month-picker.component';
import { MonthCalendarHeaderComponent } from '../components/month-picker/month-calendar-header.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MonthpickerInputDirective } from '../components/month-picker/month-picker-input.directive';

@NgModule({
  declarations: [MonthPickerComponent, MonthCalendarHeaderComponent, MonthpickerInputDirective],
  imports: [
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    FormsModule,
    ReactiveFormsModule,
  ],
  exports: [
    MonthPickerComponent,
    MonthCalendarHeaderComponent,
    MonthpickerInputDirective,
  ],
})
export class MonthPickerModule {}
