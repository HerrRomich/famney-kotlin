import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, ElementRef, inject, ViewChild } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { MatAutocomplete, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatChipInputEvent } from '@angular/material/chips';
import { AccountsFacade } from '@famoney-features/accounts/stores/accounts/accounts.facade';

@Component({
  selector: 'fm-account-tags-popup',
  templateUrl: 'accounts-filter-popup.component.html',
  styleUrls: ['accounts-filter-popup.component.scss'],
})
export class AccountTagsPopupComponent {
  accountsFacade = inject(AccountsFacade);
  separatorKeysCodes: number[] = [ENTER, COMMA];
  @ViewChild('tagsInput', { static: true }) tagsInput!: ElementRef<HTMLInputElement>;
  @ViewChild('tagAutoComplete', { static: true }) matAutocomplete!: MatAutocomplete;
  filter = new FormGroup({
    'range': new FormGroup({
      start: new FormControl<Date | null>(null),
      end: new FormControl<Date | null>(null),
    }),
  });

  selectTag(event: MatAutocompleteSelectedEvent) {
    this.accountsFacade.addTagToSelection(event.option.viewValue);
    this.tagsInput.nativeElement.value = '';
  }

  addTag(event: MatChipInputEvent) {
    if (this.matAutocomplete.isOpen) {
      return;
    }
    const input = event.chipInput.inputElement;
    const value = event.value;
    if (this.matAutocomplete.options.filter((option) => option.value === value.trim()).length !== 1) {
      return;
    }
    this.accountsFacade.addTagToSelection(value.trim());
    if (input) {
      input.value = '';
    }
  }

  removeTag(tag: string) {
    this.accountsFacade.removeTagFromSelection(tag);
  }

  clearSelectedTags() {
    this.accountsFacade.clearSelectedTags();
  }
}
