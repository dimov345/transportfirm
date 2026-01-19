import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';
import { AuthResponse, LoginRequest } from './auth.models';

const TOKEN_KEY = 'auth_token';
const EMAIL_KEY = 'auth_email';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = 'http://localhost:8080/api';

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  private readonly _isAuthed$ = new BehaviorSubject<boolean>(this.hasToken());

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
            localStorage.setItem(TOKEN_KEY, res.token);
            localStorage.setItem(EMAIL_KEY, res.email);
          }
          this._isAuthed$.next(true);
        })
      );
  }

  logout(): void {
    if (this.isBrowser) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(EMAIL_KEY);
    }
    this._isAuthed$.next(false);
  }

  getToken(): string | null {
    if (!this.isBrowser) return null;
    return localStorage.getItem(TOKEN_KEY);
  }

  getEmail(): string | null {
    if (!this.isBrowser) return null;
    return localStorage.getItem(EMAIL_KEY);
  }

  isAuthenticated(): boolean {
    return this.hasToken();
  }

  private hasToken(): boolean {
    if (!this.isBrowser) return false;
    const t = localStorage.getItem(TOKEN_KEY);
    return !!t && t.trim().length > 0;
  }
}
