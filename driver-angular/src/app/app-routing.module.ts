import { NgModule } from '@angular/core';
import { Routes, RouterModule, UrlSegment } from '@angular/router';
import { MapComponent } from './map/map.component'
import { LoginComponent } from './login/login.component'
import { LogoutComponent } from './logout/logout.component'
import { IndexComponent } from './index/index.component'
import { InputComponent } from './input/input.component'
import { ListComponent } from './list/list.component'
import { LocationComponent } from './location/location.component'
import { WebComponent } from './web/web.component'

/* export function initializeApp(url){
  return {
    consumed:url
  }
} */

const routes: Routes = [
  { path: 'map', component: MapComponent },
  { path: 'login', component: LoginComponent },
  { path: 'logout', component: LogoutComponent },
  { path: 'location', component: LocationComponent },
  { path: 'index.html', component: InputComponent },
  // { matcher: initializeApp, component: InputComponent }, 
  { path: '', component: InputComponent },
  { path: 'input', component: InputComponent },
  { path: 'list', component: ListComponent },
  { path: 'web', component: WebComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
