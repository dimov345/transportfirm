import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../services/notification.service';

/**
 * Global error interceptor — shows a toast for:
 *   0   → network / CORS error
 *   500+ → server-side errors
 *
 * 401, 403 and 404 are intentionally left to the auth interceptor
 * and individual components to handle.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notify    = inject(NotificationService);
  const platformId = inject(PLATFORM_ID);

  if (!isPlatformBrowser(platformId)) {
    return next(req);
  }

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      const status = err.status;

      if (status === 0) {
        // Network error or server unreachable
        notify.error('Няма връзка със сървъра. Провери интернет връзката си.');
      } else if (status >= 500) {
        // Server-side error — show the backend message if available
        const msg: string =
          err.error?.message ||
          err.error?.error ||
          `Сървърна грешка (${status}). Опитай отново по-късно.`;
        notify.error(msg);
      }
      // 401, 403, 404 — handled by authInterceptor / components
      return throwError(() => err);
    })
  );
};
