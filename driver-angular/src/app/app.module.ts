
import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MapComponent } from './map/map.component';

import { LeafletModule } from '@asymmetrik/ngx-leaflet';
import { LoginComponent } from './login/login.component';
import { HttpClientModule } from '@angular/common/http';
import { MultiTranslateHttpLoader } from 'ngx-translate-multi-http-loader';

import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FilterbarComponent } from './filterbar/filterbar.component';
import { IndexComponent } from './index/index.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatNativeDateModule } from '@angular/material/core'; 
import { MatDatepickerModule } from '@angular/material/datepicker'; 
import { MatSelectModule } from '@angular/material/select'; 
import { MatFormFieldModule } from '@angular/material/form-field'; 
import { MatInputModule } from '@angular/material/input'; 
import { MatButtonModule } from '@angular/material/button';
import { HammerModule } from "@angular/platform-browser";
import { DatePipe, LocationStrategy, HashLocationStrategy } from '@angular/common';
import { InputComponent } from './input/input.component';
import { NgxMaterialTimepickerModule } from 'ngx-material-timepicker';
import { ListComponent } from './list/list.component';
import { LocationComponent } from './location/location.component';
import { LogoutComponent } from './logout/logout.component';
import { OrderedFieldsPipe } from './ordered-fields.pipe';
import { WebComponent } from './web/web.component';
import { HttpClient } from '@angular/common/http'
import {TransPipe} from './input/trans.pipe'

export function HttpLoaderFactory(httpClient: HttpClient) {
  return new MultiTranslateHttpLoader(httpClient, [
    { prefix: "./assets/i18n/", suffix: ".json" },
  ]);
}
@NgModule({
  declarations: [
    AppComponent,
    MapComponent,
    LoginComponent,
    FilterbarComponent,
    IndexComponent,
    InputComponent,
    ListComponent,
    LocationComponent,
    LogoutComponent,
    OrderedFieldsPipe,
    WebComponent,
    TransPipe
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    LeafletModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatNativeDateModule,
    MatButtonModule,
    HammerModule,
    NgxMaterialTimepickerModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    }),
  ],
  providers: [DatePipe, {provide: LocationStrategy, useClass: HashLocationStrategy}],
  bootstrap: [AppComponent],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
})
export class AppModule { }
