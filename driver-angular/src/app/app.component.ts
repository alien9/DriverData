import { Component } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  constructor(private translate: TranslateService) {
    let lang=localStorage.getItem("language") || 'en'
    translate.setDefaultLang(lang);
    console.log(lang)
  }
  title = 'DRIVER';
  useLanguage(language: string): void {
    this.translate.use(language);
  }
}
