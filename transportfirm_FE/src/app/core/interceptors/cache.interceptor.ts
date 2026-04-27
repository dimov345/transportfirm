import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { of, tap } from 'rxjs';

interface CacheEntry {
  response: HttpResponse<unknown>;
  expiry: number;
}

const cache = new Map<string, CacheEntry>();

/** Cache TTL in ms — cached responses are served stale-free within this window. */
const TTL_MS = 30_000;

/**
 * Caches GET responses for 30 seconds.
 * Skips caching for auth endpoints and non-GET requests.
 * Auto-invalidates the entire cache on any successful mutation (POST/PUT/DELETE/PATCH).
 */
export const cacheInterceptor: HttpInterceptorFn = (req, next) => {
  // Auto-invalidate cache on successful mutations so the next GET is always fresh.
  if (req.method !== 'GET') {
    return next(req).pipe(
      tap(event => {
        if (event instanceof HttpResponse && event.status >= 200 && event.status < 300) {
          cache.clear();
        }
      })
    );
  }

  // Skip caching for auth routes — always fetch fresh tokens/user data
  if (req.url.includes('/auth/')) {
    return next(req);
  }

  const key = req.urlWithParams;
  const cached = cache.get(key);

  if (cached && Date.now() < cached.expiry) {
    return of(cached.response.clone());
  }

  return next(req).pipe(
    tap(event => {
      if (event instanceof HttpResponse && event.status === 200) {
        cache.set(key, {
          response: event.clone(),
          expiry: Date.now() + TTL_MS
        });
      }
    })
  );
};

/**
 * Manually clear the cache, optionally scoped to a URL fragment.
 * The interceptor already clears the full cache automatically on successful mutations,
 * so manual calls are only needed for edge cases (e.g. optimistic updates).
 */
export function invalidateCache(urlFragment?: string): void {
  if (!urlFragment) {
    cache.clear();
    return;
  }
  for (const key of cache.keys()) {
    if (key.includes(urlFragment)) {
      cache.delete(key);
    }
  }
}
