import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);
  const isBrowser = isPlatformBrowser(platformId);

  // SSR: skip credentials entirely
  if (!isBrowser) {
    return next(req);
  }

  // Attach credentials so the browser sends the httpOnly JWT cookie automatically.
  // No Authorization header needed — the cookie is the auth mechanism.
  const cloned = req.clone({ withCredentials: true });

  return next(cloned).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err?.status === 401) {
        const isLoginRequest = req.url.includes('/auth/login');
        const alreadyOnLogin = router.url.startsWith('/login');

        if (!isLoginRequest && !alreadyOnLogin) {
          auth.logout();
          router.navigate(['/login'], { queryParams: { returnUrl: router.url } });
        }

        return throwError(() => err);
      }

      // 403 firstLoginRequired → redirect to first-login flow
      const firstLoginRequired =
        err?.status === 403 &&
        (err?.error?.firstLoginRequired === true || err?.error?.firstLoginRequired === 'true');

      const isFirstLoginRequest = req.url.includes('/auth/first-login');
      const alreadyOnFirstLogin = router.url.startsWith('/first-login');

      if (firstLoginRequired && !isFirstLoginRequest && !alreadyOnFirstLogin) {
        const emailFromBody = (req.body as any)?.email?.trim?.();
        const email = emailFromBody || auth.getEmail?.() || '';
        const msg = err?.error?.message || 'Първо влизане: трябва да смениш паролата.';

        router.navigate(['/first-login'], {
          queryParams: email ? { email } : {},
          state: { message: msg }
        });
      }

      return throwError(() => err);
    })
  );
};
