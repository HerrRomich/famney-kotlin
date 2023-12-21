import { inject, Injectable } from '@angular/core';
import { AbstractControl, NonNullableFormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { ParseNumberService } from '@famoney-shared/services/parse-numbers.service';

export type EntryItemFormGroup = ReturnType<EntryItemService['createEntryItemFormGroup']>;

@Injectable()
export class EntryItemService {

  private formBuilder = inject(NonNullableFormBuilder);
  private parseNumberService = inject(ParseNumberService);

  createEntryItemFormGroup() {
    return this.formBuilder.group({
      categoryId: this.formBuilder.control<number | string | undefined>(
        { value: undefined, disabled: false },
        Validators.required,
      ),
      amount: this.formBuilder.control<number | undefined>({ value: undefined, disabled: false }, [
        Validators.required,
        this.validateAmountNotZero(),
      ]),
      comments: this.formBuilder.control<string | undefined>({
        value: undefined,
        disabled: false,
      }),
    });
  }

  validateAmountNotZero() {
    return (control: AbstractControl): ValidationErrors => {
      const amount = typeof control.value === 'number' ? control.value : this.parseNumberService.parse(control.value);
      if (Number.isNaN(amount)) {
        return { wrongFormat: 'Should be number!' };
      } else if (amount === 0) {
        return { zeroValue: 'Should not be zero!' };
      } else {
        return {};
      }
    };
  }
}
