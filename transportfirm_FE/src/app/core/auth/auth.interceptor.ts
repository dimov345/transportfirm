import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const token = auth.getToken();

  // винаги пращаме credentials (за cookie jwt), и ако има token -> Authorization header
  const cloned = req.clone({
    withCredentials: true,
    setHeaders: token ? { Authorization: `Bearer ${token}` } : {}
  });

  return next(cloned).pipe(
    catchError((err: HttpErrorResponse) => {
      const firstLoginRequired =
        err?.status === 403 &&
        (err?.error?.firstLoginRequired === true || err?.error?.firstLoginRequired === 'true');

      // не редиректвай ако вече сме на first-login или ако това е самият first-login request
      const isFirstLoginRequest = req.url.includes('/auth/first-login');
      const alreadyOnFirstLogin = router.url.startsWith('/first-login');

      if (firstLoginRequired && !isFirstLoginRequest && !alreadyOnFirstLogin) {
        // взимаме email от login payload ако го има
        const emailFromBody = (req.body as any)?.email?.trim?.();
        const email = emailFromBody || auth.getEmail() || '';

        const msg =
          err?.error?.message || 'Първо влизане: трябва да смениш паролата, преди да продължиш.';

        router.navigate(['/first-login'], {
          queryParams: email ? { email } : {},
          state: { message: msg }
        });
      }

      return throwError(() => err);
    })
  );
};
