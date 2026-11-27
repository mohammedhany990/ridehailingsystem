import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { TPipe } from '../../shared/t.pipe';
import { AUTH_STYLES } from './auth.styles';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TPipe],
  template: `
    <div class="auth-wrap">
      <div class="glow a"></div><div class="glow b"></div>
      <div class="card">
        <h1 class="logo">Ry<span>d</span>e</h1>
        <p class="subtitle">{{ 'auth.login' | t }}</p>
        @if (error()) {
          <div class="error-banner" role="alert">{{ error() }}</div>
        }
        <form [formGroup]="form" (ngSubmit)="submit()">
          <div class="field">
            <label for="email">{{ 'auth.email' | t }}</label>
            <input id="email" type="email" formControlName="email" autocomplete="email" />
          </div>
          <div class="field">
            <label for="password">{{ 'auth.password' | t }}</label>
            <input id="password" type="password" formControlName="password" autocomplete="current-password" />
          </div>
          <button class="submit" type="submit" [disabled]="form.invalid || loading()">
            {{ loading() ? ('common.loading' | t) + '…' : ('auth.login' | t) }}
          </button>
        </form>
        <p class="alt">{{ 'auth.noAccount' | t }} <a routerLink="/register">{{ 'auth.register' | t }}</a></p>
      </div>
    </div>
  `,
  styles: AUTH_STYLES,
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  readonly loading = signal(false);
  readonly error = signal('');

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  submit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.error.set('');
    const { email, password } = this.form.getRawValue();
    this.auth.login(email, password).subscribe({
      next: () => this.router.navigateByUrl('/'),
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message ?? 'Login failed');
      },
    });
  }
}
