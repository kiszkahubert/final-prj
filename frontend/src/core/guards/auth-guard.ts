import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AccountService } from '../services/account-service';
import { ToastService } from '../services/toast-service';

export const authGuard: CanActivateFn = () => {
  const accountService = inject(AccountService);
  const toast = inject(ToastService);
  const router = inject(Router);

  if (accountService.isAuthenticated()) return true;
  else {
    toast.error('You shall not pass');
    return router.createUrlTree(['/login']);
  }
};
