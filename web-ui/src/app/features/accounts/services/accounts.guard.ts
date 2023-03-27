import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateChildFn, Router, RouterStateSnapshot } from '@angular/router';
import { map, tap } from 'rxjs/operators';
import { AccountsService } from './accounts.service';

export const canAtivateAccount: CanActivateChildFn  = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  const accountId = parseInt(route.params['accountId'],  10) || 0;
  const accountsService = inject(AccountsService);
  const router = inject(Router);
  return accountsService.accounts$.pipe(
    map(accounts => {
      let result = accounts.find(value => value.id === accountId);
      if (!result) {
        result = accounts.find(value => value.id === accountsService.selectedAccountId);
      }
      if (!result) {
        return accounts[0].id ?? 0;
      }
      return result.id;
    }),
    tap(value => (accountsService.selectedAccountId = value)),
    tap(value => {
      if (value !== accountId) {
        router.navigate([
          route.parent?.pathFromRoot
            .map(ars => ars.url.map(segment => segment.path).join('/'))
            .filter(part => part.length !== 0)
            .join('/'),
          value,
        ]);
      }
    }),
    map(() => true),
  );

}
