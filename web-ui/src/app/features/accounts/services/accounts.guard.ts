import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateChildFn, Router, RouterStateSnapshot } from '@angular/router';
import { map, tap } from 'rxjs/operators';
import { AccountsStore } from '../store/accounts.store';

export const canAtivateAccount: CanActivateChildFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  const accountId = parseInt(route.params['accountId'], 10) || 0;
  const accountsStore = inject(AccountsStore);
  const router = inject(Router);
  return accountsStore.filteredAccounts$.pipe(
    map(accounts => {
      const found = accounts.some(account => account.id ===accountId);
      if (found) {
        accountsStore.selectAccount(accountId);
      } else {
        const accountIdQuery = accounts.length > 0 ? `/${accounts[0].id}` : '';
        router.navigateByUrl(`/accounts${accountIdQuery}`);
      }
      return found;
    }),
    tap(result => {
      if (result) {
      } else {
      }
    }),
  );
};
