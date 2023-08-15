import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateChildFn, Router } from '@angular/router';
import { AccountsFacade } from '@famoney-features/accounts/stores/accounts/accounts.facade';
import { Observable, OperatorFunction, pipe, UnaryFunction } from 'rxjs';
import { filter, map } from 'rxjs/operators';

function filterNullish<T>(): UnaryFunction<Observable<T | null | undefined>, Observable<T>> {
  return pipe(filter((x) => x !== null && x !== undefined) as OperatorFunction<T | null | undefined, T>);
}

export const canActivateAccount: CanActivateChildFn = (route: ActivatedRouteSnapshot) => {
  const accountId = parseInt(route.params['accountId'], 10) || 0;
  const accountsFacade = inject(AccountsFacade);
  const router = inject(Router);
  return accountsFacade.filteredAccounts$.pipe(
    filterNullish(),
    map((accounts) => {
      const found = accounts.some((account) => account.id === accountId);
      if (found) {
        accountsFacade.selectAccount(accountId);
      } else if (accounts.length > 0) {
        const accountIdQuery = `/${accounts[0].id}`;
        router.navigateByUrl(`/accounts${accountIdQuery}`);
      }
      return found;
    }),
  );
};
