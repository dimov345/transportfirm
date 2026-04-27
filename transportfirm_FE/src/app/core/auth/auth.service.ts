import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';
import { AuthResponse, LoginRequest } from './auth.models';
import { environment } from '../../../environments/environment';

// Only non-sensitive metadata stored in localStorage for UI purposes.
// The JWT itself lives in the httpOnly cookie set by the server.
const EMAIL_KEY = 'auth_email';
const ROLE_KEY  = 'auth_role';
const IAT_KEY   = 'auth_iat';   // token issued-at  (Unix seconds, UI only)
const EXP_KEY   = 'auth_exp';   // token expires-at (Unix seconds, UI only)

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = environment.apiUrl;

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  private readonly _isAuthed$ = new BehaviorSubject<boolean>(this.hasSession());

  constructor(private http: HttpClient) {}

  isAuthed$() {
    return this._isAuthed$.asObservable();
  }

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/login`, payload, { withCredentials: true })
      .pipe(
        tap((res) => {
          if (this.isBrowser) {
            // Store only non-sensitive display metadata — token stays in httpOnly cookie
            localStorage.setItem(EMAIL_KEY, res.email);
            const role = this.decodeRole(res.token);
            if (role) localStorage.setItem(ROLE_KEY, role);
            // Cache iat/exp as plain timestamps for the profile page UI
            const payload = this.decodePayload(res.token);
            if (payload?.['iat']) localStorage.setItem(IAT_KEY, String(payload['iat']));
            if (payload?.['exp']) localStorage.setItem(EXP_KEY, String(payload['exp']));
          }
          this._isAuthed$.next(true);
        })
      );
  }

  logout(): void {
    // Revoke token server-side (increments tokenVersion + clears httpOnly cookie)
    this.http.post(`${this.baseUrl}/auth/logout`, {}, { withCredentials: true })
      .subscribe({ error: () => {} }); // fire-and-forget — clear local state regardless

    if (this.isBrowser) {
      localStorage.removeItem(EMAIL_KEY);
      localStorage.removeItem(ROLE_KEY);
      localStorage.removeItem(IAT_KEY);
      localStorage.removeItem(EXP_KEY);
    }
    this._isAuthed$.next(false);
  }

  getEmail(): string | null {
    if (!this.isBrowser) return null;
    return localStorage.getItem(EMAIL_KEY);
  }

  getRole(): string | null {
    if (!this.isBrowser) return null;
    return localStorage.getItem(ROLE_KEY);
  }

  hasRole(...roles: string[]): boolean {
    const role = this.getRole();
    return role !== null && roles.includes(role);
  }

  isAuthenticated(): boolean {
    return this.hasSession();
  }

  /** Returns the token issued-at as Unix seconds, or null if not available. */
  getTokenIat(): number | null {
    if (!this.isBrowser) return null;
    const v = localStorage.getItem(IAT_KEY);
    return v ? Number(v) : null;
  }

  /** Returns the token expires-at as Unix seconds, or null if not available. */
  getTokenExp(): number | null {
    if (!this.isBrowser) return null;
    const v = localStorage.getItem(EXP_KEY);
    return v ? Number(v) : null;
  }

  private hasSession(): boolean {
    if (!this.isBrowser) return false;
    const email = localStorage.getItem(EMAIL_KEY);
    return !!email && email.trim().length > 0;
  }

  private decodePayload(token: string): Record<string, unknown> | null {
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch {
      return null;
    }
  }

  private decodeRole(token: string): string | null {
    try {
      const payload = this.decodePayload(token);
      if (!payload) return null;
      const authorities: string[] = (payload['authorities'] as string[]) ?? (payload['roles'] as string[]) ?? [];
      const found = authorities.find((a: string) => a.startsWith('ROLE_'));
      return found ? found.replace('ROLE_', '') : null;
    } catch {
      return null;
    }
  }
}
