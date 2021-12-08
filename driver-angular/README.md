# DriverAngular

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 10.0.2.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).

## DRIVER Android Client Deployment

Run `ng build --prod`
`./deploy.sh`

The app will be placed under DriverData assets for consumption by Android DriverData app.

## Languages

The languages are loaded from the client's native language. The fallback language is English (en).
To add a new language, create its proper json file at `src/assets/i18n/<language_code>.json` with the lowercase code for the language. 

Run 

`npm run extract-translations`

The dictionaries will be updated with the necessary terms for the interface. The newly added terms will have empty string as their value, and it must be properly updated.