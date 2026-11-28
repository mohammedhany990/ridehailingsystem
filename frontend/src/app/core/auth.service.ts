import { Injectable, computed, inject, signal } from '@angular/core';
import { ApiService } from './api.service';
import { Observable, tap } from 'rxjs';
import { AuthResponse, Profile } from './models';

const TOKEN_KEY = 'rh.access_token';
const REFRESH_KEY = 'rh.refresh_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = inject(ApiService);

  private tokenSignal = signal<string | null>(this.readToken());
  readonly isAuthenticated = computed(() => !!this.tokenSignal());
  readonly currentUserId = computed(() => {
    const payload = this.decodePayload();
    return (payload?.['userId'] as string) ?? null;
  });
  readonly currentRole = computed(() => {
    const payload = this.decodePayload();
    return (payload?.['role'] as string) ?? null;
  });

  private decodePayload(): Record<string, unknown> | null {
    const token = this.tokenSignal();
    if (!token) return null;
    try {
      return JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    } catch {
      return null;
    }
  }

  private readToken(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY);
    } catch {
      return null;
    }
  }

  token(): string | null {
    return this.tokenSignal();
  }

  register(name: string, email: string, password: string, phone: string, role: 'RIDER' | 'DRIVER'): Observable<unknown> {
    return this.api.post('/api/auth/register', { name, email, password, phone, role });
  }

  verifyEmail(email: string, otp: string): Observable<unknown> {
    return this.api.post('/api/auth/verify-email', { email, otp });
  }

  resendOtp(email: string): Observable<unknown> {
    return this.api.post('/api/auth/resend-verification-otp', { email });
  }

  loginWithGoogle(idToken: string): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('/api/auth/google', { idToken }).pipe(
      tap((res) => {
        this.tokenSignal.set(res.accessToken);
        localStorage.setItem(TOKEN_KEY, res.accessToken);
        if (res.refreshToken) localStorage.setItem(REFRESH_KEY, res.refreshToken);
      }),
    );
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.api.post<AuthResponse>('/api/auth/login', { email, password }).pipe(
      tap((res) => {
        this.tokenSignal.set(res.accessToken);
        localStorage.setItem(TOKEN_KEY, res.accessToken);
        if (res.refreshToken) localStorage.setItem(REFRESH_KEY, res.refreshToken);
      }),
    );
  }

  logout(): void {
    this.tokenSignal.set(null);
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
  }
}
