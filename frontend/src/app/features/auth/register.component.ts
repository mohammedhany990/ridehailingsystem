import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { TPipe } from '../../shared/t.pipe';
import { AUTH_STYLES } from './auth.styles';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TPipe],
  template: `
    <div class="auth-wrap">
      <div class="glow a"></div><div class="glow b"></div>
      <div class="card">
        <h1 class="logo">Ry<span>d</span>e</h1>
        <p class="subtitle">{{ 'auth.register' | t }}</p>
        @if (error()) {
          <div class="error-banner" role="alert">{{ error() }}</div>
        }
        <form [formGroup]="form" (ngSubmit)="submit()">
          <div class="field">
            <label for="name">{{ 'auth.name' | t }}</label>
            <input id="name" formControlName="name" autocomplete="name" />
          </div>
          <div class="field">
            <label for="email">{{ 'auth.email' | t }}</label>
            <input id="email" type="email" formControlName="email" autocomplete="email" />
          </div>
          <div class="field">
            <label for="phone">{{ 'auth.phone' | t }}</label>
            <input id="phone" formControlName="phone" autocomplete="tel" inputmode="tel" />
          </div>
          <div class="field">
            <label for="password">{{ 'auth.password' | t }}</label>
            <input id="password" type="password" formControlName="password" autocomplete="new-password" />
          </div>
          <button class="submit" type="submit" [disabled]="form.invalid || loading()">
            {{ loading() ? ('common.loading' | t) + '…' : ('auth.register' | t) }}
          </button>
        </form>
        <p class="alt">{{ 'auth.hasAccount' | t }} <a routerLink="/login">{{ 'auth.login' | t }}</a></p>
      </div>
    </div>
  `,
  styles: AUTH_STYLES,
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  readonly loading = signal(false);
  readonly error = signal('');

  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.pattern(/^\+?[0-9]{7,15}$/)]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  submit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.error.set('');
    const { name, email, phone, password } = this.form.getRawValue();
    this.auth.register(name, email, password, phone, 'RIDER').subscribe({
      next: () => this.router.navigate(['/verify'], { queryParams: { email } }),
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message ?? 'Registration failed');
      },
    });
  }
}
