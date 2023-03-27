import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ApiModule as AccountsApiModule, Configuration as AccountsApiConfiguration } from '@famoney-apis/accounts';
import { ApisModule } from '@famoney-shared/modules/apis.module';
import { MaterialModule } from '@famoney-shared/modules/material.module';
import { SharedModule } from '@famoney-shared/modules/shared.module';
import { SimpleNotificationsModule } from 'angular2-notifications';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MaterialModule,
    SharedModule,
    SimpleNotificationsModule.forRoot({
      timeOut: 5000,
    }),
    ApisModule,
  ],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {}
