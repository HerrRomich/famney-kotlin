import { HostBinding } from '@angular/core';
import { Directive, Input, ElementRef, Renderer2, HostListener } from '@angular/core';

@Directive({
  selector: '[fmSpacer]',
})
export class SpacerDirective {
  @HostBinding('class')
  elementClass = 'fm-spacer';
}
