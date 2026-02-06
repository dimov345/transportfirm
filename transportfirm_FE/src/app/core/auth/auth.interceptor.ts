import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

const TOKEN_KEY = 'auth_token';

function readTokenBrowserOnly(): string | null {
  const t = localStorage.getItem(TOKEN_KEY);
  return t && t.trim().length > 0 ? t : null;
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const platformId = inject(PLATFORM_ID);
  const isBrowser = isPlatformBrowser(platformId);

  // SSR: не пипаме localStorage, не пращаме credentials, не добавяме Bearer
  if (!isBrowser) {
    return next(req);
  }

  const token = readTokenBrowserOnly();

  const cloned = req.clone({
    withCredentials: true,
    setHeaders: token ? { Authorization: `Bearer ${token}` } : {}
  });

  return next(cloned).pipe(
    catchError((err: HttpErrorResponse) => {
      // --- 401 -> redirect към login ---
      /*if (err?.status === 401) {
        const isLoginRequest = req.url.includes('/auth/login');
        const alreadyOnLogin = router.url.startsWith('/login');

        // избягваме loop ако самият login endpoint връща 401
        if (!isLoginRequest && !alreadyOnLogin) {
          // по желание: чистим токена
          localStorage.removeItem(TOKEN_KEY);
          // ако AuthService държи state:
          auth.logout?.(); // ако имаш такава функция, иначе махни този ред

          router.navigate(['/login'], {
            queryParams: { returnUrl: router.url }
          });
        }

        return throwError(() => err);
      }*/

      // --- 403 firstLoginRequired -> redirect към first-login ---
      const firstLoginRequired =
        err?.status === 403 &&
        (err?.error?.firstLoginRequired === true || err?.error?.firstLoginRequired === 'true');

      const isFirstLoginRequest = req.url.includes('/auth/first-login');
      const alreadyOnFirstLogin = router.url.startsWith('/first-login');

      if (firstLoginRequired && !isFirstLoginRequest && !alreadyOnFirstLogin) {
        const emailFromBody = (req.body as any)?.email?.trim?.();
        const email = emailFromBody || auth.getEmail?.() || '';

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
