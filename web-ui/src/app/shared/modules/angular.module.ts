import { ScrollingModule } from '@angular/cdk/scrolling';
import { CommonModule, DecimalPipe } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

const ANGULAR_MODULES = [
  FormsModule,
  CommonModule,
  ReactiveFormsModule,
  ScrollingModule,
  HttpClientModule,
];

@NgModule({
  imports: ANGULAR_MODULES,
  exports: [...ANGULAR_MODULES],
  providers:[
    {
      provide: DecimalPipe
    }
  ]
})
export class AngularModule {}
