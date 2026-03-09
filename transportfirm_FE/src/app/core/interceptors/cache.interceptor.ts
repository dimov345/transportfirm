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
 * Invalidation happens automatically by TTL — mutation endpoints (POST/PUT/DELETE)
 * do NOT need to clear the cache manually since list data refreshes on next visit.
 */
export const cacheInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.method !== 'GET') {
    return next(req);
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

/** Call this after a mutation (create/update/delete) to force fresh data on next load. */
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
