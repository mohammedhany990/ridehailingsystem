import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { AdminService, PaymentStats, RideStats, UserStats } from '../../core/admin.service';
import { TPipe } from '../../shared/t.pipe';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [DecimalPipe, TPipe],
  template: `
    <div class="page">
      <h1>{{ 'admin.title' | t }}</h1>
      @if (loading()) {
        <p class="dim">{{ 'common.loading' | t }}…</p>
      } @else {
        <h2>{{ 'admin.users' | t }}</h2>
        <div class="grid">
          <div class="card"><span class="label">{{ 'admin.totalUsers' | t }}</span><span class="value">{{ users()?.totalUsers }}</span></div>
          <div class="card"><span class="label">{{ 'admin.riders' | t }}</span><span class="value">{{ users()?.riders }}</span></div>
          <div class="card"><span class="label">{{ 'admin.drivers' | t }}</span><span class="value">{{ users()?.drivers }}</span></div>
          <div class="card"><span class="label">{{ 'admin.activeUsers' | t }}</span><span class="value">{{ users()?.activeUsers }}</span></div>
        </div>
        <h2>{{ 'admin.rides' | t }}</h2>
        <div class="grid">
          <div class="card"><span class="label">{{ 'admin.totalRides' | t }}</span><span class="value">{{ rides()?.totalRides }}</span></div>
          <div class="card"><span class="label">{{ 'admin.ridesToday' | t }}</span><span class="value">{{ rides()?.ridesToday }}</span></div>
          <div class="card"><span class="label">{{ 'admin.activeRides' | t }}</span><span class="value">{{ rides()?.activeRides }}</span></div>
        </div>
        @if (rides(); as r) {
          <div class="status-list">
            @for (entry of statusEntries(r); track entry[0]) {
              <div class="status-row">
                <span class="status" [attr.data-status]="entry[0]">{{ entry[0] }}</span>
                <span class="count">{{ entry[1] }}</span>
              </div>
            }
          </div>
        }
        <h2>{{ 'admin.revenue' | t }}</h2>
        <div class="grid">
          <div class="card"><span class="label">{{ 'admin.totalRevenue' | t }}</span><span class="value">{{ payments()?.totalRevenue | number: '1.2-2' }} <small>{{ payments()?.currency }}</small></span></div>
          <div class="card"><span class="label">{{ 'admin.revenueToday' | t }}</span><span class="value">{{ payments()?.revenueToday | number: '1.2-2' }} <small>{{ payments()?.currency }}</small></span></div>
          <div class="card"><span class="label">{{ 'admin.succeededPayments' | t }}</span><span class="value">{{ payments()?.succeededPayments }}</span></div>
          <div class="card"><span class="label">{{ 'admin.failedPayments' | t }}</span><span class="value">{{ payments()?.failedPayments }}</span></div>
        </div>
      }
    </div>
  `,
  styles: `
    .page {
      padding: calc(env(safe-area-inset-top, 0px) + 76px) 18px 110px;
      max-width: 640px; margin: 0 auto; animation: fade-up var(--dur-med) var(--ease-out);
    }
    h1 { font-size: 24px; letter-spacing: -0.02em; margin: 0 0 18px; }
    h2 { font-size: 15px; color: var(--text-dim); margin: 22px 0 10px; }
    .dim { color: var(--text-dim); }
    .grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }
    .card {
      background: var(--bg-card); border: 1px solid var(--line); border-radius: var(--radius-md);
      padding: 14px 12px; display: flex; flex-direction: column; gap: 6px;
    }
    .label { font-size: 12px; color: var(--text-dim); }
    .value { font-size: 22px; font-weight: 700; letter-spacing: -0.01em; }
    .value small { font-size: 11px; font-weight: 500; color: var(--text-dim); }
    .status-list { display: flex; flex-direction: column; gap: 6px; margin-top: 10px; }
    .status-row {
      display: flex; justify-content: space-between; align-items: center;
      background: var(--bg-card); border: 1px solid var(--line); border-radius: var(--radius-md);
      padding: 10px 16px; font-size: 14px;
    }
    .status-row .count { font-weight: 700; }
  `,
})
export class AdminComponent implements OnInit {
  private admin = inject(AdminService);

  readonly loading = signal(true);
  readonly users = signal<UserStats | null>(null);
  readonly rides = signal<RideStats | null>(null);
  readonly payments = signal<PaymentStats | null>(null);

  ngOnInit(): void {
    let pending = 3;
    const done = () => {
      pending -= 1;
      if (pending <= 0) this.loading.set(false);
    };
    this.admin.userStats().subscribe({ next: (s) => this.users.set(s), error: done, complete: done });
    this.admin.rideStats().subscribe({ next: (s) => this.rides.set(s), error: done, complete: done });
    this.admin.paymentStats().subscribe({ next: (s) => this.payments.set(s), error: done, complete: done });
  }

  statusEntries(r: RideStats): [string, number][] {
    return Object.entries(r.ridesByStatus ?? {});
  }
}
