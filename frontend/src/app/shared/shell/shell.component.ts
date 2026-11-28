import { Component, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { TPipe } from '../t.pipe';
import { ThemeService } from '../../core/theme.service';
import { I18nService } from '../../core/i18n.service';
import { NotificationService } from '../../core/notification.service';
import { AuthService } from '../../core/auth.service';
import { ProfileService } from '../../core/profile.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, TPipe],
  template: `
    <div class="shell">
      <header class="topbar">
        <span class="brand">{{ 'app.name' | t }}</span>
        <div class="top-actions">
          <button class="icon-btn" type="button" (click)="i18n.toggle()" [attr.aria-label]="'language'">
            {{ i18n.lang() === 'en' ? 'ع' : 'EN' }}
          </button>
          <button class="icon-btn" type="button" (click)="theme.toggle()" aria-label="theme">
            @if (theme.theme() === 'dark') {
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="4"/><path d="M12 2v2m0 16v2M4.9 4.9l1.4 1.4m11.4 11.4 1.4 1.4M2 12h2m16 0h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg>
            } @else {
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12.8A9 9 0 1 1 11.2 3 7 7 0 0 0 21 12.8Z"/></svg>
            }
          </button>
        </div>
      </header>
      <main class="content">
        <router-outlet />
      </main>
      @if (notifications.toast(); as toast) {
        <div class="toast" role="status">
          <strong>{{ toast.title }}</strong>
          <span>{{ toast.body }}</span>
        </div>
      }
      <nav class="bottom-nav" [attr.aria-label]="'main'">
        <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 10.5 12 3l9 7.5V21H3Z"/></svg>
          <span>{{ 'nav.home' | t }}</span>
        </a>
        <a routerLink="/trips" routerLinkActive="active">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 3"/></svg>
          <span>{{ 'nav.trips' | t }}</span>
        </a>
        @if (auth.currentRole() === 'DRIVER') {
          <a routerLink="/earnings" routerLinkActive="active">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2v20M17 6.5c0-2-2.2-3-5-3s-5 1-5 3 2 2.8 5 3.5 5 1.5 5 3.5-2.2 3-5 3-5-1-5-3"/></svg>
            <span>{{ 'nav.earnings' | t }}</span>
          </a>
        }
        @if (auth.currentRole() === 'ADMIN') {
          <a routerLink="/admin" routerLinkActive="active">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="9" rx="1"/><rect x="14" y="3" width="7" height="5" rx="1"/><rect x="14" y="12" width="7" height="9" rx="1"/><rect x="3" y="16" width="7" height="5" rx="1"/></svg>
            <span>{{ 'nav.admin' | t }}</span>
          </a>
        }
        <a routerLink="/profile" routerLinkActive="active">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="8" r="4"/><path d="M4 21c1.5-4 5-6 8-6s6.5 2 8 6"/></svg>
          <span>{{ 'nav.profile' | t }}</span>
        </a>
      </nav>
    </div>
  `,
  styles: `
    .shell { display: flex; flex-direction: column; height: 100dvh; }
    .topbar {
      position: absolute; top: 0; left: 0; right: 0; z-index: 500;
      display: flex; justify-content: space-between; align-items: center;
      padding: calc(env(safe-area-inset-top, 0px) + 14px) 18px 14px;
      pointer-events: none;
    }
    .topbar > * { pointer-events: auto; }
    .brand {
      font-weight: 800; font-size: 22px; letter-spacing: -0.02em;
      background: var(--bg-glass); backdrop-filter: blur(14px);
      padding: 6px 14px; border-radius: var(--radius-lg); border: 1px solid var(--line);
    }
    .top-actions { display: flex; gap: 8px; }
    .icon-btn {
      width: 40px; height: 40px; border-radius: 50%;
      background: var(--bg-glass); backdrop-filter: blur(14px);
      border: 1px solid var(--line); color: var(--text);
      display: grid; place-items: center; font-weight: 700; font-size: 13px;
      transition: transform var(--dur-fast) var(--ease-out), background var(--dur-fast);
    }
    .icon-btn:hover { transform: translateY(-1px); }
    .icon-btn:active { transform: scale(0.94); }
    .content { flex: 1; min-height: 0; position: relative; }
    .toast {
      position: absolute; top: calc(env(safe-area-inset-top, 0px) + 76px); inset-inline: 16px; z-index: 600;
      background: var(--bg-elev); border: 1px solid var(--brand); border-radius: var(--radius-md);
      padding: 13px 16px; box-shadow: var(--shadow-lg); display: flex; flex-direction: column; gap: 2px;
      animation: pop var(--dur-med) var(--ease-out);
    }
    .toast strong { font-size: 14px; }
    .toast span { font-size: 12.5px; color: var(--text-dim); }
    .bottom-nav {
      position: absolute; bottom: 0; left: 0; right: 0; z-index: 500;
      display: flex; justify-content: space-around;
      padding: 10px 12px calc(env(safe-area-inset-bottom, 0px) + 12px);
      background: var(--bg-glass); backdrop-filter: blur(18px);
      border-top: 1px solid var(--line);
    }
    .bottom-nav a {
      display: flex; flex-direction: column; align-items: center; gap: 3px;
      color: var(--text-faint); text-decoration: none; font-size: 11px; font-weight: 600;
      padding: 4px 14px; border-radius: var(--radius-md);
      transition: color var(--dur-fast), transform var(--dur-fast) var(--ease-spring);
    }
    .bottom-nav a.active { color: var(--brand-strong); }
    [data-theme='light'] .bottom-nav a.active { color: var(--brand); }
    .bottom-nav a:active { transform: scale(0.92); }
  `,
})
export class ShellComponent implements OnInit {
  readonly theme = inject(ThemeService);
  readonly i18n = inject(I18nService);
  readonly notifications = inject(NotificationService);
  readonly auth = inject(AuthService);
  private profile = inject(ProfileService);

  ngOnInit(): void {
    this.notifications.refreshUnread().subscribe({ error: () => {} });
    this.notifications.load().subscribe({ error: () => {} });
    this.notifications.connect();
    this.profile.load().subscribe({ error: () => {} });
  }
}
