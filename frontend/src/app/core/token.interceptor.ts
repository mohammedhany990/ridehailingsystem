import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

const PUBLIC_PATHS = [
  '/api/auth/register',
  '/api/auth/login',
  '/api/auth/verify-email',
  '/api/auth/resend-verification-otp',
  '/api/auth/forgot-password',
  '/api/auth/reset-password',
];

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  if (PUBLIC_PATHS.some((p) => req.url.includes(p))) {
    return next(req);
  }
  const token = inject(AuthService).token();
  if (!token) return next(req);
  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
