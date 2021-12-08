import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'trans'
})
export class TransPipe implements PipeTransform {
  transform(value: string, dict:object): string {
    console.log('translating')
    if(dict[value]) return dict[value]
    return value
  }
}
