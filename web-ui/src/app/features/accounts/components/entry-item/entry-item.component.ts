import { ChangeDetectionStrategy, Component, DestroyRef, OnInit, signal, ViewEncapsulation } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ControlContainer } from '@angular/forms';
import { EntryCategoryDto } from '@famoney-apis/master-data/model/entry-category.dto';
import {
  EntryItemFormGroup,
  EntryItemService,
} from '@famoney-features/accounts/components/entry-item/entry-item.service';
import { EntryCategoryService, FlatEntryCategory } from '@famoney-shared/services/entry-category.service';
import { combineLatestWith, debounceTime, map, startWith, switchMap } from 'rxjs/operators';

interface EntryCategoryWithFilterOption extends FlatEntryCategory {
  optionName: string;
}

export type EntryCategoriesForVisualisation = {
  readonly flatEntryCategories: Map<number, FlatEntryCategory>;
  readonly expenses: EntryCategoryWithFilterOption[];
  readonly incomes: EntryCategoryWithFilterOption[];
};

@Component({
  selector: '[formGroup] fm-entry-item, [formGroupName] fm-entry-item',
  templateUrl: 'entry-item.component.html',
  styleUrls: ['entry-item.component.scss'],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EntryItemComponent implements OnInit {
  formGroup = this.entryItemService.createEntryItemFormGroup();

  entryCategories = signal<EntryCategoriesForVisualisation | undefined>(undefined);
  categoryPath = signal<string | undefined>(undefined);
  entryItemClass = signal<string | undefined>(undefined);

  constructor(
    private entryItemService: EntryItemService,
    private entryCategoriesService: EntryCategoryService,
    private controlContainer: ControlContainer,
    private destroyRef: DestroyRef,
  ) {}

  ngOnInit() {
    if (this.controlContainer.control) {
      this.formGroup = this.controlContainer.control as EntryItemFormGroup;
    } else throw Error('formGroup is not set.');
    this.formGroup.controls.categoryId.valueChanges
      .pipe(
        debounceTime(350),
        startWith(this.formGroup.controls.categoryId.value),
        switchMap((filterValue) =>
          this.entryCategoriesService.entryCategoriesForVisualisation$.pipe(
            map((entryCategories) => {
              const filterText =
                (typeof filterValue === 'number'
                  ? entryCategories.flatEntryCategories.get(filterValue)?.name
                  : filterValue) ?? '';
              const filter = new RegExp(filterText, 'i');
              return {
                flatEntryCategories: entryCategories.flatEntryCategories,
                expenses: this.filterCategories(filter, entryCategories.flatEntryCategories, entryCategories.expenses),
                incomes: this.filterCategories(filter, entryCategories.flatEntryCategories, entryCategories.incomes),
              };
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((categories) => {
        this.entryCategories.set(categories);
      });
    this.formGroup.controls.categoryId.valueChanges
      .pipe(
        startWith(this.formGroup.controls.categoryId.value),
        combineLatestWith(this.entryCategoriesService.entryCategoriesForVisualisation$),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(([categoryId, flatEntryCategories]) => {
        const entryItemCategory =
          typeof categoryId === 'number' ? flatEntryCategories?.flatEntryCategories.get(categoryId) : undefined;
        if (entryItemCategory?.type === 'EXPENSE') {
          this.entryItemClass.set('fm-expense-category');
        } else if (entryItemCategory?.type === 'INCOME') {
          this.entryItemClass.set('fm-income-category');
        } else {
          this.entryItemClass.set(undefined);
        }
        this.categoryPath.set(entryItemCategory?.fullPath);
      });
  }

  filterCategories(
    filter: RegExp,
    flatEntryCategories: Map<number, FlatEntryCategory>,
    entryCategories?: EntryCategoryDto[],
  ): EntryCategoryWithFilterOption[] {
    const filteredEntryCategories = entryCategories?.reduce((filteredCategories, entryCategory) => {
      const flattenEntryCategory = entryCategory.id ? flatEntryCategories.get(entryCategory.id) : undefined;
      const subCategories = this.filterCategories(filter, flatEntryCategories, entryCategory.children);
      if ((filter.test(entryCategory.name) || subCategories.length > 0) && flattenEntryCategory) {
        filteredCategories.push({
          ...flattenEntryCategory,
          optionName:
            entryCategory.name.match(filter)?.join().length ?? 0 > 0
              ? entryCategory.name.replace(filter, (subString) => `<mark>${subString}</mark>`)
              : entryCategory.name,
        });
        filteredCategories.push(...subCategories);
      }
      return filteredCategories;
    }, new Array<EntryCategoryWithFilterOption>());
    return filteredEntryCategories ?? [];
  }

  getCategoryName() {
    return (categoryId: number): string => this.entryCategories()?.flatEntryCategories.get(categoryId)?.name ?? '';
  }

  getCategoryErrorMessageCode() {
    const categoryControl = this.formGroup?.get('categoryId');
    if (categoryControl?.getError('required')) {
      return 'accounts.entryItem.fields.category.errors.required';
    } else {
      return undefined;
    }
  }

  getAmountErrorMessageCode() {
    const amountControl = this.formGroup.get('amount');
    if (amountControl?.hasError('required')) {
      return 'accounts.entryItem.fields.amount.errors.required';
    } else if (amountControl?.hasError('wrongFormat')) {
      return 'accounts.entryItem.fields.amount.errors.wrongFormat';
    } else if (amountControl?.hasError('zeroValue')) {
      return 'accounts.entryItem.fields.amount.errors.zeroValue';
    } else {
      return undefined;
    }
  }
}
