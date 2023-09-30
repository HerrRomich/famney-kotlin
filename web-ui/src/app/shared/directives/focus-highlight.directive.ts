import { Directive, ElementRef, HostListener, Input, Renderer2 } from '@angular/core';

@Directive({
  selector: '[fmFocusHighlight]',
})
export class FocusHighlightDirective {
  @Input('fmFocusHighlight') highlightClass = '';

  constructor(
    private el: ElementRef,
    private renderer: Renderer2,
  ) {}

  @HostListener('focusin')
  onFocusIn() {
    this.renderer.addClass(this.el.nativeElement, this.highlightClass);
  }

  @HostListener('focusout')
  onFocusOut() {
    this.renderer.removeClass(this.el.nativeElement, this.highlightClass);
  }
}
