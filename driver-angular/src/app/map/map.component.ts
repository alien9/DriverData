import { Component, OnInit, Input } from '@angular/core';
import * as L from 'leaflet';
import { LeafletModule } from '@asymmetrik/ngx-leaflet';
import { RecordService } from '../record.service';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { Observable, Subscription } from 'rxjs';
import { utfGrid } from '../../UtfGrid';
import { DatePipe } from '@angular/common';
import {ElementRef, ViewChild, AfterViewInit} from '@angular/core';


@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements OnInit {
  @Input() filterUpdate: Observable<void>;
  @ViewChild('map') elementView: ElementRef;
  
  private filterSubscription: Subscription;
  private map;
  options: any;
  recordtype: Object;
  layersControl: any;
  layers: any=[];
  overlays: any=[];
  osm: L.TileLayer;
  sat: L.TileLayer;
  pts: L.LayerGroup;
  hea: L.TileLayer;
  dot: L.TileLayer;
  seg: L.TileLayer;
  seggrid: any;
  grid: any;

  mapHeight: string="100%";
  
  controlOptions:any;
  constructor(
    private recordService: RecordService,
    private router: Router,
    private datePipe: DatePipe
  ) { }

  ngOnInit(): void {
    this.osm=L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 18, attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors' });
    this.sat = L.tileLayer('http://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {});

    //this.controlOptions={collapsed:true};
    this.options = {
      zoom: environment.zoom,
      center: environment.center,
      layers:[
        this.osm
      ]
    }
    this.layersControl={
      baseLayers: {
        'Open Street Map': this.osm,
        'Satellite': this.sat
      }, overlays:{

      }
    }
    this.layers=[this.sat];
    this.recordService.getRecordType().subscribe((data) => this.getTileKey(data), (err) => this.escape(err));
    if(this.filterUpdate){
      this.filterSubscription = this.filterUpdate.subscribe((res) => {
        res['occurred_max']=res['occurred_max'].toISOString(); 
        res['occurred_min']=res['occurred_min'].toISOString(); 
        if(!res['weather'].length){
          delete res['weather'];
        }
        this.recordService.getTileKey(this.recordtype, res).subscribe((data) => this.load(data), (err) => this.escape(err));
      });
    }

  }
  escape(err: any): void {
    console.log(err);
    if (err.status == 403) {
      this.router.navigateByUrl('/login?returnUrl=map');
    }
  }
  getTileKey(data: any[]): void {
    for(var i=0;i<data['results'].length; i++){
      if(data['results'][i]['label'] == environment.label){
        this.recordtype = data['results'][i];
      }
    }
    this.recordService.getTileKey(this.recordtype, null).subscribe((data) => this.load(data), (err) => this.escape(err));
  }
  load(data: any[]): void {
    if(!this.grid){
      this.grid = utfGrid(environment.mapserver + 'RecordsGrid@SphericalMercator/{z}/{x}/{-y}.grid.json?tilekey=' + data['tilekey']+'&recordtype='+this.recordtype['uuid']+'&map=/etc/mapserver/records.map', {
        pointerCursor:true
      });
      this.grid.on("click", function (e) {
        console.log(e)
      });
    }else{
      this.grid.setUrl(environment.mapserver + 'RecordsGrid@SphericalMercator/{z}/{x}/{-y}.grid.json?tilekey=' + data['tilekey']+'&recordtype='+this.recordtype['uuid']+'&map=/etc/mapserver/records.map');
    }
    if(!this.dot){
      this.dot=L.tileLayer(environment.mapserver + 'Records@SphericalMercator/{z}/{x}/{-y}.png?tilekey=' + data['tilekey']+'&recordtype='+this.recordtype['uuid']+'&map=/etc/mapserver/records.map', {
        detectRetina: true,
        attribution: '&copy; DRIVER contributors'
      });
    }else{
      this.dot.setUrl(environment.mapserver + 'Records@SphericalMercator/{z}/{x}/{-y}.png?tilekey=' + data['tilekey']+'&recordtype='+this.recordtype['uuid']+'&map=/etc/mapserver/records.map');
    }
    if(!this.hea){
      this.hea=L.tileLayer(environment.mapserver + 'tiles/table/grout_record/id/' + this.recordtype['uuid'] + '/{z}/{x}/{y}.png?heatmap=true&tilekey=' + data['tilekey'], {
        detectRetina: true,
        attribution: '&copy; DRIVER contributors'
      })
    }else{
      this.hea.setUrl(environment.mapserver + 'tiles/table/grout_record/id/' + this.recordtype['uuid'] + '/{z}/{x}/{y}.png?heatmap=true&tilekey=' + data['tilekey']);
    }
    this.seggrid = utfGrid(environment.mapserver + 'SegmentsGrid@SphericalMercator/{z}/{x}/{-y}.grid.json?tilekey=' + data['tilekey']+'&map=/etc/mapserver/segments.map', {
      pointerCursor:true
    });
    this.seggrid.on("click", function (e) {
      console.log(e)
      let data=JSON.parse(e.data.data);
      console.log(data);
      alert(data.name);
    });
          
    if(!Object.keys(this.layersControl.overlays).length){
      console.log("creating");
      this.layersControl.overlays= {
        'Heatmap': this.hea,
        'Pontos': L.layerGroup([
          this.grid,
          this.dot
        ]),
        'Segments': new L.LayerGroup([
          L.tileLayer(environment.mapserver + 'Segments@SphericalMercator/{z}/{x}/{-y}.png?tilekey=' + data['tilekey']+'&map=/etc/mapserver/segments.map', {
            detectRetina: true,
            attribution: '&copy; DRIVER contributors'
          }),
          this.seggrid
        ])
      };
    }else{
      this.dot.redraw();
    }
    //this.overlays=[pts];
    
  }
  ngAfterViewInit() {
//    this.mapHeight = this.elementView.nativeElement.offsetHeight+300+'px';
    //this.mapHeight = '600px';
    console.log(this);
  }
  onResize(e):void{
    console.log(e);
    //console.log(this.elementView.nativeElement.offsetHeight);


  }
}
