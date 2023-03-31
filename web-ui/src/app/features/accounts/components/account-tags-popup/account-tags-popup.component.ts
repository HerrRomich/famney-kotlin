import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, ViewChild, ElementRef } from '@angular/core';
import { Observable, combineLatest } from 'rxjs';
import { FormControl } from '@angular/forms';
import { MatAutocomplete, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatChipInputEvent } from '@angular/material/chips';
import { map, startWith } from 'rxjs/operators';
import { AccountsStore } from '@famoney-features/accounts/store/accounts.store';

@Component({
  selector: 'fm-account-tags-popup',
  templateUrl: 'account-tags-popup.component.html',
  styleUrls: ['account-tags-popup.component.scss'],
})
export class AccountTagsPopupComponent {
  separatorKeysCodes: number[] = [ENTER, COMMA];
  public filtersAccountTags$: Observable<string[]>;
  @ViewChild('tagsInput', { static: true }) tagsInput!: ElementRef<HTMLInputElement>;
  tagsCtrl = new FormControl();
  @ViewChild('tagAutoComplete', { static: true }) matAutocomplete!: MatAutocomplete;

  constructor(public accountsStore: AccountsStore) {
    this.filtersAccountTags$ = combineLatest([
      this.accountsStore.tags$,
      this.accountsStore.selectedTags$,
    ]).pipe(map(([tagsList, selectedTags]) => tagsList.filter(tag => !selectedTags.includes(tag))));
  }

  selectTag(event: MatAutocompleteSelectedEvent) {
    this.accountsStore.addTagToSelection(event.option.viewValue);
    this.tagsInput.nativeElement.value = '';
    this.tagsCtrl.setValue(null);
  }

  addTag(event: MatChipInputEvent) {
    if (this.matAutocomplete.isOpen) {
      return;
    }
    const input = event.input;
    const value = event.value;
    if (this.matAutocomplete.options.filter(option => option.value === value.trim()).length !== 1) {
      return;
    }
    this.accountsStore.addTagToSelection(value.trim());
    if (input) {
      input.value = '';
    }
    this.tagsCtrl.setValue(null);
  }

  removeTag(tag: string) {
    this.accountsStore.removeTagFromSelection(tag);
  }

  clearTags() {
    this.accountsStore.clearSelectedTags();
  }
}
