import { Component, OnInit, EventEmitter, Output, Input } from '@angular/core';
import * as L from 'leaflet';
import 'leaflet.locatecontrol'


import { environment } from '../../environments/environment';

const iconRetinaUrl = './assets/marker-icon-2x.png';
const iconUrl = './assets/marker-icon.png';
const shadowUrl = './assets/marker-shadow.png';
const iconDefault = L.icon({
  iconRetinaUrl,
  iconUrl,
  shadowUrl,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  tooltipAnchor: [16, -28],
  shadowSize: [41, 41]
});
L.Marker.prototype.options.icon = iconDefault;


@Component({
  selector: 'app-location',
  templateUrl: './location.component.html',
  styleUrls: ['./location.component.css']
})
export class LocationComponent implements OnInit {
  osm: L.TileLayer;
  options: any;
  layersControl: any;
  layers: any = [];
  controlOptions: any;
  position: L.LatLng
  @Input() latitude: number
  @Input() longitude: number
  private map:L.Map

  constructor() { }

  @Output() locateEvent = new EventEmitter<object>();
  ngOnInit(): void {
    this.osm = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 18, attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors' });
    let c=localStorage.getItem("config")
    let center = [-23.5, -46.6]
    if(c){
      let config=JSON.parse(c)
      center=[config.MAP_CENTER_LATITUDE,config.MAP_CENTER_LONGITUDE]
    }
    if (this.latitude && this.longitude) {
      center = [this.latitude, this.longitude]
    } else if (localStorage.getItem("position")) {
      center = (localStorage.getItem("position")).split(/\t/).map((item) => parseFloat(item))
    }
    this.controlOptions = { collapsed: true };
    this.options = {
      zoom: 16,
      center: center,
      layers: [
        this.osm
      ]
    }
    this.layersControl = {
      baseLayers: {
        'Open Street Map': this.osm
      }, overlays: {

      }
    }
    if (this.latitude && this.longitude) {
      this.setLocation({
        "latlng": new L.LatLng(this.latitude, this.longitude)
      })
    }
  }
  setLocation(e) {
    this.position = e.latlng
    this.layers = [new L.Marker([this.position.lat, this.position.lng])];
  }
  save() {
    this.locateEvent.emit(this.position)
  }
  cancel() {
    this.locateEvent.emit(null)
  }
  onMapReady(map) {
    //L.control.locate().addTo(map);
    this.map=map
  }
  setPosition(p) {
    if (p && p.target && p.target.value) {
      var ll = p.target.value.split(/,/)
      this.position = new L.LatLng(ll[0], ll[1])
      this.layers = [new L.Marker([ll[0], ll[1]])]
      this.map.panTo(this.position)
      this.map.setZoom(19)
    }
  }
}
