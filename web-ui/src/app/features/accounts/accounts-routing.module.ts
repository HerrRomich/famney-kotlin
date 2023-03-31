import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AccountTableComponent } from './pages/account-table/account-table.component';
import { AccountsComponent } from './pages/accounts/accounts.component';
import { canAtivateAccount } from './services/accounts.guard';

const routes: Routes = [
  {
    path: '',
    component: AccountsComponent,
    children: [
      {
        path: '',
        redirectTo: '0',
        pathMatch: 'full',
      },
      {
        path: ':accountId',
        component: AccountTableComponent,
        canActivate: [canAtivateAccount],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AccountsRoutingModule {}
