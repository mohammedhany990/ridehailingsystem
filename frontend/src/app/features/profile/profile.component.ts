import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ProfileService } from '../../core/profile.service';
import { AuthService } from '../../core/auth.service';
import { TPipe } from '../../shared/t.pipe';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, DecimalPipe, TPipe],
  template: `
    <div class="page">
      <h1>{{ 'profile.title' | t }}</h1>

      <div class="avatar-block">
        <div class="avatar" (click)="fileInput.click()" role="button" tabindex="0" (keydown.enter)="fileInput.click()">
          @if (avatarSrc()) {
            <img [src]="avatarSrc()" alt="profile photo" />
          } @else {
            <span class="initials">{{ initials() }}</span>
          }
          <span class="edit-badge">✎</span>
        </div>
        <input #fileInput type="file" accept="image/jpeg,image/png,image/webp" hidden (change)="onFile($event)" />
        <button type="button" class="link" (click)="fileInput.click()">{{ 'profile.changePhoto' | t }}</button>
        @if (avatarError()) {
          <p class="error">{{ avatarError() }}</p>
        }
      </div>

      <div class="stats">
        <div class="stat">
          <strong>{{ profile()?.rating | number: '1.1-1' }}</strong>
          <span>{{ 'profile.rating' | t }}</span>
        </div>
        <div class="stat">
          <strong>{{ profile()?.createdAt | date: 'MMM y' }}</strong>
          <span>{{ 'profile.member' | t }}</span>
        </div>
      </div>

      <form [formGroup]="form" (ngSubmit)="save()" class="form">
        <label>{{ 'auth.name' | t }}</label>
        <input formControlName="name" />
        <label>{{ 'auth.phone' | t }}</label>
        <input formControlName="phone" inputmode="tel" />
        <label>{{ 'auth.email' | t }}</label>
        <input [value]="profile()?.email" disabled />
        <button class="save" type="submit" [disabled]="form.invalid || saving()">
          {{ saving() ? ('common.loading' | t) + '…' : ('profile.save' | t) }}
        </button>
        @if (saved()) {
          <p class="saved">Saved</p>
        }
      </form>

      <button type="button" class="logout" (click)="logout()">{{ 'nav.logout' | t }}</button>
    </div>
  `,
  styles: `
    .page {
      padding: calc(env(safe-area-inset-top, 0px) + 76px) 20px 110px;
      max-width: 480px; margin: 0 auto; animation: fade-up var(--dur-med) var(--ease-out);
    }
    h1 { font-size: 24px; letter-spacing: -0.02em; margin: 0 0 20px; }
    .avatar-block { display: flex; flex-direction: column; align-items: center; gap: 8px; margin-bottom: 22px; }
    .avatar {
      width: 108px; height: 108px; border-radius: 50%; overflow: hidden; position: relative;
      background: var(--brand-soft); display: grid; place-items: center; cursor: pointer;
      border: 2px solid var(--line); transition: transform var(--dur-fast) var(--ease-spring);
    }
    .avatar:hover { transform: scale(1.03); }
    .avatar img { width: 100%; height: 100%; object-fit: cover; }
    .initials { font-size: 36px; font-weight: 800; color: var(--brand-strong); }
    .edit-badge {
      position: absolute; bottom: 4px; inset-inline-end: 4px; width: 30px; height: 30px;
      border-radius: 50%; background: var(--brand); color: #fff; display: grid; place-items: center;
      font-size: 13px; border: 2px solid var(--bg-elev);
    }
    .link { background: none; border: none; color: var(--brand-strong); font-weight: 600; font-size: 13.5px; }
    .error { color: var(--danger); font-size: 12.5px; margin: 0; }
    .stats { display: flex; gap: 10px; margin-bottom: 22px; }
    .stat {
      flex: 1; background: var(--bg-card); border: 1px solid var(--line);
      border-radius: var(--radius-md); padding: 14px; text-align: center;
    }
    .stat strong { display: block; font-size: 19px; letter-spacing: -0.01em; }
    .stat span { font-size: 12px; color: var(--text-faint); }
    .form { display: flex; flex-direction: column; }
    label { font-size: 12.5px; font-weight: 600; color: var(--text-dim); margin: 12px 0 6px 2px; }
    input {
      padding: 13px 15px; border-radius: var(--radius-md);
      background: var(--bg-card); border: 1px solid var(--line); color: var(--text);
    }
    input:focus { outline: none; border-color: var(--brand); box-shadow: var(--ring); }
    input:disabled { opacity: 0.5; }
    .save {
      margin-top: 20px; padding: 14px; border: none; border-radius: var(--radius-md);
      background: linear-gradient(135deg, var(--brand), var(--brand-strong));
      color: #fff; font-weight: 700; box-shadow: 0 10px 26px rgba(79, 124, 255, 0.35);
      transition: transform var(--dur-fast) var(--ease-spring);
    }
    .save:active:not(:disabled) { transform: scale(0.98); }
    .save:disabled { opacity: 0.55; }
    .saved { color: var(--accent); text-align: center; font-weight: 600; margin: 10px 0 0; }
    .logout {
      width: 100%; margin-top: 26px; padding: 13px; border-radius: var(--radius-md);
      border: 1px solid var(--danger-soft); background: var(--danger-soft); color: var(--danger); font-weight: 700;
    }
  `,
})
export class ProfileComponent implements OnInit {
  private profileService = inject(ProfileService);
  private auth = inject(AuthService);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  readonly profile = this.profileService.profile;
  readonly avatarSrc = signal<string | null>(null);
  readonly avatarError = signal('');
  readonly saving = signal(false);
  readonly saved = signal(false);

  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    phone: ['', [Validators.pattern(/^\+?[0-9]{7,15}$/)]],
  });

  initials(): string {
    const name = this.profile()?.name ?? '';
    return name.split(' ').map((w) => w[0]).slice(0, 2).join('').toUpperCase();
  }

  ngOnInit(): void {
    this.profileService.load().subscribe({
      next: (p) => {
        this.form.patchValue({ name: p.name, phone: p.phone ?? '' });
        if (p.avatarUrl) this.bustCache();
      },
      error: () => {},
    });
  }

  private bustCache(): void {
    const p = this.profile();
    if (p) this.avatarSrc.set(`${this.profileService.avatarUrl(p.id)}?t=${Date.now()}`);
  }

  onFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.avatarError.set('');
    if (file.size > 5 * 1024 * 1024) {
      this.avatarError.set('Photo must be 5 MB or smaller');
      return;
    }
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
      this.avatarError.set('Only JPG, PNG or WEBP images');
      return;
    }
    this.profileService.uploadAvatar(file).subscribe({
      next: () => this.bustCache(),
      error: (err) => this.avatarError.set(err?.error?.message ?? 'Upload failed'),
    });
    input.value = '';
  }

  save(): void {
    if (this.form.invalid || this.saving()) return;
    this.saving.set(true);
    this.saved.set(false);
    const { name, phone } = this.form.getRawValue();
    this.profileService.update(name, phone).subscribe({
      next: () => {
        this.saving.set(false);
        this.saved.set(true);
        setTimeout(() => this.saved.set(false), 2500);
      },
      error: () => this.saving.set(false),
    });
  }

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/login');
  }
}
