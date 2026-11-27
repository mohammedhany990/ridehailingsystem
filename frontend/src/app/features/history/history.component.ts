import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { RideService } from '../../core/ride.service';
import { Ride } from '../../core/models';
import { TPipe } from '../../shared/t.pipe';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [DatePipe, DecimalPipe, RouterLink, TPipe],
  template: `
    <div class="page">
      <h1>{{ 'trip.history' | t }}</h1>
      @if (loading()) {
        <p class="dim">{{ 'common.loading' | t }}…</p>
      } @else if (rides().length === 0) {
        <div class="empty">
          <p>{{ 'trip.empty' | t }}</p>
        </div>
      } @else {
        <div class="list">
          @for (ride of rides(); track ride.id) {
            <a class="ride-card" [routerLink]="['/trips', ride.id, 'replay']">
              <div class="row">
                <span class="status" [attr.data-status]="ride.status">{{ ride.status }}</span>
                <span class="date">{{ ride.requestedAt | date: 'MMM d, h:mm a' }}</span>
              </div>
              <div class="row">
                <span class="tier">{{ ride.vehicleType }}</span>
                @if (ride.fare) {
                  <span class="fare">{{ ride.fare.totalAmount | number: '1.2-2' }} {{ ride.fare.currency }}</span>
                }
              </div>
            </a>
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
    .list { display: flex; flex-direction: column; gap: 10px; }
    .ride-card {
      display: block; background: var(--bg-card); border: 1px solid var(--line);
      border-radius: var(--radius-md); padding: 14px 16px; text-decoration: none; color: var(--text);
      transition: transform var(--dur-fast) var(--ease-spring), border-color var(--dur-fast);
    }
    .ride-card:hover { border-color: var(--brand); }
    .ride-card:active { transform: scale(0.985); }
    .row { display: flex; justify-content: space-between; align-items: center; padding: 2px 0; }
    .status { font-size: 11.5px; font-weight: 700; letter-spacing: 0.04em; padding: 3px 10px; border-radius: 20px; }
    .status[data-status='COMPLETED'] { background: var(--accent-soft); color: var(--accent); }
    .status[data-status='CANCELLED'] { background: var(--danger-soft); color: var(--danger); }
    .status[data-status='REQUESTED'], .status[data-status='ACCEPTED'], .status[data-status='IN_PROGRESS'] {
      background: var(--brand-soft); color: var(--brand-strong);
    }
    .date { color: var(--text-faint); font-size: 12.5px; }
    .tier { color: var(--text-dim); font-size: 13px; font-weight: 600; }
    .fare { font-weight: 700; font-size: 14.5px; }
  `,
})
export class HistoryComponent implements OnInit {
  private rideService = inject(RideService);
  readonly rides = signal<Ride[]>([]);
  readonly loading = signal(true);

  ngOnInit(): void {
    this.rideService.riderHistory().subscribe({
      next: (rides) => {
        this.rides.set(rides);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
