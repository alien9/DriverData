import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { v4 as uuid } from 'uuid';
import { AuthService } from '../auth.service';
import { first } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-input',
  templateUrl: './input.component.html',
  styleUrls: ['./input.component.css']
})
export class InputComponent implements OnInit {
  @Input() existingRecord;
  private schema: object;
  private schema_uuid: string;
  public description: string;
  public tables: any[];
  public JSON: any;
  public registry: object;
  public pane: string = 'listing'
  public locating: boolean = false
  public listing: boolean = true
  public inputing: boolean = false;
  public title: string = "..."
  public validationErrorMessage: string
  private mess: string = ""
  private latitude: number;
  private longitude: number;
  public clock: string = ''
  private index: number
  public images: File[] = []
  public preview: any[] = []
  public filename: string;
  public show_photo = false

  dict: object
  isNew = false
  selectedPreview: number;
  date: Date = new Date();
  @ViewChild('form') mainForm;
  imageCallback: any[];
  debug = false
  constructor(
    private authenticationService: AuthService,
    public readonly translate: TranslateService,
  ) {
    this.JSON = JSON;
    this.registry = {
      'data': {}
    };

  }

  ngOnInit(): void {
    this.debug = (!!localStorage.getItem("debug")) || false
    let lang = localStorage.getItem("language")

    this.authenticationService.getLanguage(lang).pipe(first()).subscribe({
      next: data => {
        console.log(data)
        this.dict = data
        this.initDataset()
        localStorage.setItem('dict', JSON.stringify(this.dict))
      },
      error: err => {
        let d = localStorage.getItem('dict')
        if (!d) {
          localStorage.setItem('dict', '{}')
          this.dict = {}
        } else {
          this.dict = JSON.parse(d)
        }
        this.initDataset()
      }
    })
  }
  loadRecordSchema() {
    let recordSchema = localStorage.getItem('record_schema');
    if (!recordSchema) {
      this.pane = 'login'
      return;
    }
    this.schema = JSON.parse(recordSchema);
    this.schema_uuid = this.schema['uuid'];
    this.schema = this.schema['schema'];
  }
  initDataset() {
    this.loadRecordSchema()
    if (!this.schema) return
    let data = {};
    this.tables = Object.keys(this.schema['properties'])
      .sort((k, j) => { return this.schema['properties'][k].propertyOrder - this.schema['properties'][j].propertyOrder })
    this.tables.forEach(t => {
      data[t] = (this.schema['definitions'][t].multiple) ? [] : { "_localId": uuid() };
      let defs = this.schema['definitions'][t]
      defs['fields'] = Object.keys(defs.properties)
        .sort((k, j) => defs.properties[k].propertyOrder - defs.properties[j].propertyOrder)
        .map(function (k) {
          return { ...{ "key": k }, ...defs.properties[k] };
        })
    })
    this.registry['data'] = data;
    this.description = JSON.stringify(this.tables);
  }

  getRecordType(): string {
    return "";
  }
  ngAfterViewInit() {

  }
  cancel(): void {
    this.images = []
    this.preview = []
    this.pane = 'listing'

  }
  add(t) {
    this.registry['data'][t].push({ "_localId": uuid() });
  }
  pop(t) {
    this.registry['data'][t].pop();
  }
  write() {
    if (!this.date)
      return
    if (!this.clock)
      return

    let t = this.clock.split(/\s|:/).map((y) => parseInt(y))

    let d = this.date
    d.setHours(t[0])
    d.setMinutes(t[1])

    this.registry['occurred_from'] = d.toISOString()
    this.registry['occurred_to'] = d.toISOString()
    this.registry['schema'] = this.schema_uuid
    this.registry["geom"] = `SRID=4326;POINT(${this.longitude} ${this.latitude})`
    console.log(this.registry);
    let r = this.registry;
    let ds = localStorage.getItem("dataset");
    if (!ds) ds = '[]';
    let dataset = JSON.parse(ds);
    if (typeof this.index == 'number')
      dataset[this.index] = { record: this.registry, images: this.images, preview: this.preview }
    else
      dataset.push({ record: this.registry, images: this.images, preview: this.preview });
    localStorage.setItem("dataset", JSON.stringify(dataset));
    this.pane = 'listing'
  }
  isValid() {
    if (!this.date)
      return false
    if (!this.clock)
      return false
    if (!this.latitude || !this.longitude)
      return false
    let v = true
    this.mess = "..."
    Object.entries(this.schema['definitions']).forEach(([key, definition]) => {
      let tablename = definition['title']
      if (definition['multiple']) {
        //console.log(key)
      } else {
        Object.entries(definition['properties']).filter(([nk, fu]) => definition['required'].indexOf(nk) >= 0).forEach((f) => {
          if (!this.registry['data'][key]) {
            this.mess = `validation error: ${key}`
            v = false
          } else if (!this.registry['data'][key][f[0]]) {
            this.mess = `validation error: ${tablename} / ${f[0]}`
            v = false
          }
        })
      }
    })
    return v
  }
  locate() {
    this.pane = "locating"
  }
  setLocation(p) {
    this.pane = "inputing"
    console.log("SETTING LOCATION")
    if (p) {
      this.latitude = p.lat
      this.longitude = p.lng
    }
  }
  loadLocation(e) {
    if (e && e.target && e.target.value) {
      let ll = e.target.value.split(/,/).map((k) => parseFloat(k))
      if (ll.length == 2)
        this.setLocation({ lat: ll[0], lng: ll[1] })
    }
  }
  loadRecord(e: any) {
    if (e) {
      this.loadRecordSchema()
      this.images = e.images
      this.preview = e.preview
      this.isNew = true
      this.registry = e.record
      this.index = e.index
      this.date = new Date(Date.parse(e.record['occurred_from']))
      this.clock = `${this.date.getHours()}:${this.date.getMinutes()}`
      if (e.record.geom) {
        let cords = e.record.geom.match(/POINT\((\S+) (\S+)\)/)
        if (cords.length > 0) {
          this.longitude = cords[1]
          this.latitude = cords[2]
        }
      }
    } else {
      this.isNew = false
      this.initDataset()
      this.date = new Date()
      this.clock = `${this.date.getHours()}:${this.date.getMinutes()}`
      this.index = null
      this.latitude = null
      this.longitude = null
    }
    this.pane = 'inputing'
  }
  setTime(e) {
    console.log("setting time")
    console.log(e)
    this.clock = e
  }
  setDate(e) {
    if (e) {
      this.date = e.value
    }
    console.log("setting date")
    console.log(e)
  }
  list() {
    this.pane = 'listing'
  }
  logout() {
    this.authenticationService.logout();
    this.pane = 'login'
  }
  cross() {
    console.log("cross")
    navigator.geolocation.getCurrentPosition((position) => {
      console.log("Got position", position.coords);
      this.latitude = position.coords.latitude;
      this.longitude = position.coords.longitude;
    });
  }
  tableExists(o: any) {
    return true
  }
  onImageLoaded(e: FileList, table: any, field: string) {
    let i = 0;
    var reader = new FileReader();
    let fn = e[i].name
    reader.onloadend = function (el) {
      table[field] = reader.result
    }
    reader.readAsDataURL(e[i]);
  }
  onFileSelected(e: FileList) {
    let i = 0;
    var reader = new FileReader();

    while (i < e.length) {
      this.images.push(e[i])
      let p = this.preview
      var reader = new FileReader();
      let fn = e[i].name
      reader.onloadend = function (el) {
        p.push({ 'src': reader.result, 'filename': fn })
      }
      reader.readAsDataURL(e[i]);
      i++
    }
  }
  removePreview(i: number) {
    if (i < 0) return
    this.preview.splice(i, 1);
  }
  loadImage(e) {
    console.log("Loading an image")
    console.log(e.target.value)
    if (!e.target || !e.target.value) return
    if (this.imageCallback) {
      this.imageCallback[0][this.imageCallback[1]] = `data:image/jpeg;base64,${e.target.value}`
    } else {
      this.preview.push({ 'filename': this.filename, 'src': `data:image/jpeg;base64,${e.target.value}` })
    }
  }
  selectPreview(e) {
    this.selectedPreview = e;
  }
  setImageCallback(o: any, f: string) {
    this.imageCallback = [o, f]
  }
}
