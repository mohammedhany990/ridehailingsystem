import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { DailyEarning, EarningsService } from '../../core/earnings.service';
import { TPipe } from '../../shared/t.pipe';

@Component({
  selector: 'app-earnings',
  standalone: true,
  imports: [DatePipe, DecimalPipe, TPipe],
  template: `
    <div class="page">
      <h1>{{ 'earnings.title' | t }}</h1>
      @if (loading()) {
        <p class="dim">{{ 'common.loading' | t }}…</p>
      } @else if (days().length === 0) {
        <div class="empty">
          <p>{{ 'earnings.empty' | t }}</p>
        </div>
      } @else {
        <div class="summary">
          <div class="card">
            <span class="label">{{ 'earnings.total' | t }}</span>
            <span class="value">{{ totalAmount() | number: '1.2-2' }} <small>{{ currency() }}</small></span>
          </div>
          <div class="card">
            <span class="label">{{ 'earnings.rides' | t }}</span>
            <span class="value">{{ totalRides() }}</span>
          </div>
          <div class="card">
            <span class="label">{{ 'earnings.bestDay' | t }}</span>
            <span class="value">{{ bestDayAmount() | number: '1.2-2' }} <small>{{ currency() }}</small></span>
          </div>
        </div>
        <div class="chart" role="img" [attr.aria-label]="'earnings.chartLabel' | t">
          @for (day of chartDays(); track day.earningDate) {
            <div class="bar-col">
              <span class="bar-value">{{ day.totalAmount | number: '1.0-0' }}</span>
              <div class="bar" [style.height.%]="barHeight(day)"></div>
              <span class="bar-date">{{ day.earningDate | date: 'd MMM' }}</span>
            </div>
          }
        </div>
        <div class="list">
          @for (day of days(); track day.earningDate) {
            <div class="day-row">
              <span class="date">{{ day.earningDate | date: 'EEEE, MMM d' }}</span>
              <span class="rides">{{ day.rideCount }} {{ 'earnings.ridesUnit' | t }}</span>
              <span class="amount">{{ day.totalAmount | number: '1.2-2' }} {{ day.currency }}</span>
            </div>
          }
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
    .dim { color: var(--text-dim); }
    .empty {
      border: 1px dashed var(--line); border-radius: var(--radius-lg);
      padding: 42px 20px; text-align: center; color: var(--text-dim);
    }
    .summary { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-bottom: 20px; }
    .card {
      background: var(--bg-card); border: 1px solid var(--line); border-radius: var(--radius-md);
      padding: 14px 12px; display: flex; flex-direction: column; gap: 6px;
    }
    .label { font-size: 12px; color: var(--text-dim); }
    .value { font-size: 19px; font-weight: 700; letter-spacing: -0.01em; }
    .value small { font-size: 11px; font-weight: 500; color: var(--text-dim); }
    .chart {
      display: flex; align-items: flex-end; gap: 6px; height: 170px;
      background: var(--bg-card); border: 1px solid var(--line); border-radius: var(--radius-md);
      padding: 16px 12px 8px; margin-bottom: 20px;
    }
    .bar-col { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: flex-end; height: 100%; gap: 4px; }
    .bar {
      width: 100%; max-width: 34px; border-radius: 6px 6px 2px 2px;
      background: linear-gradient(180deg, var(--brand), var(--brand-strong));
      min-height: 3px; transition: height var(--dur-med) var(--ease-out);
    }
    .bar-value { font-size: 10px; color: var(--text-dim); }
    .bar-date { font-size: 10px; color: var(--text-dim); white-space: nowrap; }
    .list { display: flex; flex-direction: column; gap: 8px; }
    .day-row {
      display: flex; justify-content: space-between; align-items: center; gap: 10px;
      background: var(--bg-card); border: 1px solid var(--line); border-radius: var(--radius-md);
      padding: 12px 16px; font-size: 14px;
    }
    .day-row .date { flex: 1; }
    .day-row .rides { color: var(--text-dim); font-size: 12px; }
    .day-row .amount { font-weight: 600; }
  `,
})
export class EarningsComponent implements OnInit {
  private earnings = inject(EarningsService);

  readonly loading = signal(true);
  readonly days = signal<DailyEarning[]>([]);

  readonly totalAmount = computed(() => this.days().reduce((sum, d) => sum + Number(d.totalAmount), 0));
  readonly totalRides = computed(() => this.days().reduce((sum, d) => sum + Number(d.rideCount), 0));
  readonly currency = computed(() => this.days()[0]?.currency ?? 'EGP');
  readonly bestDayAmount = computed(() =>
    this.days().reduce((best, d) => Math.max(best, Number(d.totalAmount)), 0),
  );
  readonly chartDays = computed(() => this.days().slice(0, 14).reverse());

  ngOnInit(): void {
    this.earnings.daily().subscribe({
      next: (days) => {
        this.days.set(days ?? []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  barHeight(day: DailyEarning): number {
    const best = this.bestDayAmount();
    if (best <= 0) return 3;
    return Math.max(3, Math.round((Number(day.totalAmount) / best) * 100));
  }
}
