import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, ElementRef, ViewChild } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { MatAutocomplete, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatChipInputEvent } from '@angular/material/chips';
import { AccountsStore } from '@famoney-features/accounts/store/accounts.store';
import { combineLatest, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  selector: 'fm-account-tags-popup',
  templateUrl: 'accounts-filter-popup.component.html',
  styleUrls: ['accounts-filter-popup.component.scss'],
})
export class AccountTagsPopupComponent {
  separatorKeysCodes: number[] = [ENTER, COMMA];
  public filtersAccountTags$: Observable<string[]>;
  @ViewChild('tagsInput', { static: true }) tagsInput!: ElementRef<HTMLInputElement>;
  @ViewChild('tagAutoComplete', { static: true }) matAutocomplete!: MatAutocomplete;
  range = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });

  constructor(public accountsStore: AccountsStore) {
    this.filtersAccountTags$ = combineLatest([
      this.accountsStore.tags$,
      this.accountsStore.selectedTags$,
    ]).pipe(map(([tagsList, selectedTags]) => tagsList.filter(tag => !selectedTags?.includes(tag))));
  }

  selectTag(event: MatAutocompleteSelectedEvent) {
    this.accountsStore.addTagToSelection(event.option.viewValue);
    this.tagsInput.nativeElement.value = '';
  }

  addTag(event: MatChipInputEvent) {
    if (this.matAutocomplete.isOpen) {
      return;
    }
    const input = event.chipInput.inputElement;
    const value = event.value;
    if (this.matAutocomplete.options.filter(option => option.value === value.trim()).length !== 1) {
      return;
    }
    this.accountsStore.addTagToSelection(value.trim());
    if (input) {
      input.value = '';
    }
  }

  removeTag(tag: string) {
    this.accountsStore.removeTagFromSelection(tag);
  }

  clearTags() {
    this.accountsStore.clearSelectedTags();
  }
}
