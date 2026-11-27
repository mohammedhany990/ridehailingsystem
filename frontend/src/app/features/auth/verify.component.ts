import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from "@angular/router";
import { AuthService } from '../../core/auth.service';
import { TPipe } from '../../shared/t.pipe';
import { AUTH_STYLES } from './auth.styles';

@Component({
  selector: 'app-verify',
  standalone: true,
  imports: [ReactiveFormsModule, TPipe],
  template: `
    <div class="auth-wrap">
      <div class="glow a"></div><div class="glow b"></div>
      <div class="card">
        <h1 class="logo">Ry<span>d</span>e</h1>
        <p class="subtitle">{{ 'auth.verify' | t }} · {{ email }}</p>
        @if (error()) {
          <div class="error-banner" role="alert">{{ error() }}</div>
        }
        <form [formGroup]="form" (ngSubmit)="submit()">
          <div class="field">
            <label for="otp">{{ 'auth.otp' | t }}</label>
            <input id="otp" class="otp-input" formControlName="otp" inputmode="numeric" maxlength="6" autocomplete="one-time-code" />
          </div>
          <button class="submit" type="submit" [disabled]="form.invalid || loading()">
            {{ loading() ? ('common.loading' | t) + '…' : ('auth.verify' | t) }}
          </button>
        </form>
        <p class="alt"><a href="javascript:void(0)" (click)="resend()">{{ 'auth.resend' | t }}</a></p>
      </div>
    </div>
  `,
  styles: AUTH_STYLES,
})
export class VerifyComponent implements OnInit {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  email = '';
  readonly loading = signal(false);
  readonly error = signal('');

  form = this.fb.nonNullable.group({
    otp: ['', [Validators.required, Validators.pattern(/^[0-9]{6}$/)]],
  });

  ngOnInit(): void {
    this.email = this.route.snapshot.queryParamMap.get('email') ?? '';
    if (!this.email) this.router.navigateByUrl('/register');
  }

  submit(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.error.set('');
    this.auth.verifyEmail(this.email, this.form.getRawValue().otp).subscribe({
      next: () => this.router.navigateByUrl('/login'),
      error: (err) => {
        this.loading.set(false);
        this.error.set(err?.error?.message ?? 'Verification failed');
      },
    });
  }

  resend(): void {
    this.auth.resendOtp(this.email).subscribe({ error: () => {} });
  }
}
