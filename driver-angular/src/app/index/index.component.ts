import { Component, OnInit } from '@angular/core';
import { from } from 'rxjs';
import { FilterbarComponent } from '../filterbar/filterbar.component'
import { MapComponent } from '../map/map.component'
import { ViewChild } from '@angular/core'
import { Subject } from 'rxjs';
import { AuthService } from '../auth.service';
import { first } from 'rxjs/operators';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-index',
  templateUrl: './index.component.html',
  styleUrls: ['./index.component.css']
})
export class IndexComponent {
  filterSubject: Subject<void> = new Subject<void>();
  private state:string="map";
  constructor(
    private authenticationService: AuthService,
    private router: Router,        
    private route: ActivatedRoute,
) {
 
}

  ngOnInit(): void {
  }
  updateFilter(data){
    this.filterSubject.next(data);
  }
  logout(){
    this.authenticationService.logout();
    this.router.navigate(['login'], { relativeTo: this.route });
  }
  isMap(){
    return true;
  }
}
