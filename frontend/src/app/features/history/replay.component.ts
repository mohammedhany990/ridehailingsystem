import { Component, OnDestroy, OnInit, ViewChild, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { MapComponent } from '../../shared/map/map.component';
import { RideService } from '../../core/ride.service';
import { Ride, Trail } from '../../core/models';
import { TPipe } from '../../shared/t.pipe';

@Component({
  selector: 'app-replay',
  standalone: true,
  imports: [MapComponent, TPipe, RouterLink, DecimalPipe],
  template: `
    <app-map #map />
    <a class="back" routerLink="/trips">← {{ 'common.back' | t }}</a>
    <div class="panel">
      <h2>{{ 'trip.replay' | t }}</h2>
      @if (ride(); as r) {
        <p class="meta">{{ r.vehicleType }} · {{ r.status }}</p>
        @if (r.fare) {
          <p class="fare">{{ r.fare.totalAmount | number: '1.2-2' }} {{ r.fare.currency }}</p>
        }
      }
      @if (trail(); as t) {
        @if (t.pointCount > 1) {
          <input
            class="scrubber" type="range" min="0" max="100" [value]="progress()"
            (input)="scrub($any($event.target).valueAsNumber)"
            aria-label="trip replay progress" />
          <button type="button" class="play" (click)="togglePlay()">{{ playing() ? '⏸' : '▶' }}</button>
        } @else {
          <p class="meta">No trail recorded for this trip yet.</p>
        }
      }
      @if (receiptText()) {
        <p class="receipt">{{ receiptText() }}</p>
      }
    </div>
  `,
  styles: `
    :host { display: block; position: absolute; inset: 0; }
    .back {
      position: absolute; top: calc(env(safe-area-inset-top, 0px) + 76px); inset-inline-start: 16px; z-index: 400;
      background: var(--bg-glass); backdrop-filter: blur(14px); border: 1px solid var(--line);
      padding: 9px 16px; border-radius: var(--radius-lg); color: var(--text);
      text-decoration: none; font-weight: 600; font-size: 13.5px;
    }
    .panel {
      position: absolute; bottom: 0; left: 0; right: 0; z-index: 400;
      background: var(--bg-elev); border: 1px solid var(--line); border-bottom: none;
      border-radius: var(--radius-xl) var(--radius-xl) 0 0;
      padding: 18px 20px calc(env(safe-area-inset-bottom, 0px) + 88px);
      text-align: center; box-shadow: var(--shadow-lg); animation: pop var(--dur-med) var(--ease-out);
    }
    h2 { margin: 0 0 6px; font-size: 18px; }
    .meta { color: var(--text-dim); font-size: 13px; margin: 2px 0; }
    .fare { font-size: 24px; font-weight: 800; margin: 6px 0; }
    .scrubber { width: 100%; accent-color: var(--brand); margin: 12px 0 8px; }
    .play {
      width: 52px; height: 52px; border-radius: 50%; border: none; font-size: 18px;
      background: linear-gradient(135deg, var(--brand), var(--brand-strong)); color: #fff;
      box-shadow: 0 10px 26px rgba(79, 124, 255, 0.35);
      transition: transform var(--dur-fast) var(--ease-spring);
    }
    .play:active { transform: scale(0.92); }
    .receipt { color: var(--accent); font-size: 13px; font-weight: 600; margin: 8px 0 0; }
  `,
})
export class ReplayComponent implements OnInit, OnDestroy {
  @ViewChild('map') map!: MapComponent;

  private route = inject(ActivatedRoute);
  private rideService = inject(RideService);

  readonly ride = signal<Ride | null>(null);
  readonly trail = signal<Trail | null>(null);
  readonly progress = signal(0);
  readonly playing = signal(false);
  readonly receiptText = signal('');

  private timer?: ReturnType<typeof setInterval>;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;
    this.rideService.get(id).subscribe({
      next: (ride) => {
        this.ride.set(ride);
        queueMicrotask(() => {
          this.map.setPickup(ride.pickupLat, ride.pickupLng);
          this.map.setDropoff(ride.dropoffLat, ride.dropoffLng);
          this.rideService.route(ride.pickupLat, ride.pickupLng, ride.dropoffLat, ride.dropoffLng).subscribe({
            next: (res) => {
              this.map.drawRoute(res.route.geometry);
              this.map.fitAll();
            },
            error: () => this.map.fitAll(),
          });
        });
      },
      error: () => {},
    });
    this.rideService.trail(id).subscribe({
      next: (trail) => {
        this.trail.set(trail);
        if (trail.pointCount > 0) {
          queueMicrotask(() => this.render(100));
        }
      },
      error: () => {},
    });
    this.rideService.receipt(id).subscribe({
      next: (r) => this.receiptText.set(`${r.receiptNumber} · ${r.amount} ${r.currency} · ${r.status}`),
      error: () => {},
    });
  }

  scrub(value: number): void {
    this.playing.set(false);
    if (this.timer) clearInterval(this.timer);
    this.render(value);
  }

  togglePlay(): void {
    if (this.playing()) {
      this.playing.set(false);
      if (this.timer) clearInterval(this.timer);
      return;
    }
    if (this.progress() >= 100) this.progress.set(0);
    this.playing.set(true);
    this.timer = setInterval(() => {
      const next = this.progress() + 1;
      if (next >= 100) {
        this.render(100);
        this.playing.set(false);
        clearInterval(this.timer);
      } else {
        this.render(next);
      }
    }, 60);
  }

  private render(progress: number): void {
    this.progress.set(progress);
    const trail = this.trail();
    if (!trail) return;
    this.map.drawTrail(trail.points, progress / 100);
    if (trail.points.length) {
      const idx = Math.min(trail.points.length - 1, Math.floor((trail.points.length - 1) * (progress / 100)));
      const p = trail.points[idx];
      this.map.setDriver(p.lat, p.lng);
    }
  }

  ngOnDestroy(): void {
    if (this.timer) clearInterval(this.timer);
  }
}
