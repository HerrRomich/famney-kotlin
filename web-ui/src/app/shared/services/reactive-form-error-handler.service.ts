import { inject, Injectable } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { Observable, of, switchMap } from 'rxjs';
import { startWith } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class ReactiveFormErrorHandlerService {
  private translateService = inject(TranslateService);

  provideErrorHandler$(control: AbstractControl, translationPrefix: string): Observable<string | undefined> {
    return control.statusChanges.pipe(
      startWith('INVALID'),
      switchMap(() => {
        const errorKeys = control.errors ? Object.keys(control.errors) : [];
        const errorKey = errorKeys[0];
        if (errorKey) {
          return this.translateService.get(translationPrefix + '.' + errorKey, control.errors?.[errorKey]);
        } else {
          return of(undefined);
        }
      }),
    );
  }
}
