import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MonthCalendarHeaderComponent } from '../components/month-picker/month-calendar-header.component';
import { MonthpickerInputDirective } from '../components/month-picker/month-picker-input.directive';
import { MonthPickerComponent } from '../components/month-picker/month-picker.component';

@NgModule({
  declarations: [MonthPickerComponent, MonthCalendarHeaderComponent, MonthpickerInputDirective],
  imports: [MatDatepickerModule, MatFormFieldModule, MatInputModule, MatButtonModule, FormsModule, ReactiveFormsModule],
  exports: [MonthPickerComponent, MonthCalendarHeaderComponent, MonthpickerInputDirective],
})
export class MonthPickerModule {}
