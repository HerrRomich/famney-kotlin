import { Directive, HostBinding } from '@angular/core';

@Directive({
  selector: '[fmSpacer]',
})
export class SpacerDirective {
  @HostBinding('class')
  elementClass = 'fm-spacer';
}
