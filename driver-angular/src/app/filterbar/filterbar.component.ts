import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import {FormControl, FormGroupDirective, FormGroup, NgForm, Validators} from '@angular/forms';

@Component({
  selector: 'app-filterbar',
  templateUrl: './filterbar.component.html',
  styleUrls: ['./filterbar.component.scss']
})
export class FilterbarComponent implements OnInit {
  @Output() updateFilter = new EventEmitter();
  changed: boolean;
  //filterForm = new FormControl();
  range: any;
  weather_options: string[]=[
    'clear-day',
    'clear-night',
    'cloudy',
    'fog',
    'hail',
    'partly-cloudy-day',
    'partly-cloudy-night',
    'rain',
    'sleet',
    'snow',
    'thunderstorm',
    'tornado',
    'wind',
    'thunderstorm with light rain',
    'thunderstorm with rain',
    'thunderstorm with heavy rain',
    'light thunderstorm',
    'thunderstorm',
    'heavy thunderstorm',
    'ragged thunderstorm',
    'thunderstorm with light drizzle',
    'thunderstorm with drizzle',
    'thunderstorm with heavy drizzle',
    'light intensity drizzle',
    'drizzle',
    'heavy intensity drizzle',
    'light intensity drizzle rain',
    'drizzle rain',
    'heavy intensity drizzle rain',
    'shower rain and drizzle',
    'heavy shower rain and drizzle',
    'shower drizzle',
    'light rain',
    'moderate rain',
    'heavy intensity rain',
    'very heavy rain',
    'extreme rain',
    'freezing rain',
    'light intensity shower rain',
    'shower rain',
    'heavy intensity shower rain',
    'ragged shower rain',
    'light snow',
    'Snow',
    'Heavy snow',
    'Sleet',
    'Light shower sleet',
    'Shower sleet',
    'Light rain and snow',
    'Rain and snow',
    'Light shower snow',
    'Shower snow',
    'Heavy shower snow',
    'mist',
    'Smoke',
    'Haze',
    'sand/ dust whirls',
    'fog',
    'sand',
    'dust',
    'volcanic ash',
    'squalls',
    'tornado',
    'clear sky',
    'few clouds: 11-25%',
    'scattered clouds: 25-50%',
    'broken clouds: 51-84%',
    'overcast clouds: 85-100%'
  ];


  filterForm:any = new FormGroup({
    occurred_min: new FormControl('', [
      Validators.required
    ]),
    occurred_max: new FormControl('', [
      Validators.required
    ]),
    weather: new FormControl('', [
    ])
  });


  constructor() { }

  ngOnInit(): void {
  }
  isChanged(): boolean {
    return this.changed;    
  }
  setChanged():void {
    this.changed=true;
  }
  onSubmit():void {
    if(this.filterForm.status=='VALID'){
      this.updateFilter.emit(this.filterForm.value);
    }
    this.changed=false;
    
  }

}
