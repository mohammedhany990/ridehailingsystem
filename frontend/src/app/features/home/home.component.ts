import { Component, OnDestroy, ViewChild, inject, signal, computed } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { Router } from '@angular/router';
import { forkJoin, Subscription, interval, switchMap } from 'rxjs';
import { MapComponent } from '../../shared/map/map.component';
import { RideService } from '../../core/ride.service';
import { TPipe } from '../../shared/t.pipe';
import { FareEstimate, Ride } from '../../core/models';
import { ApiService } from '../../core/api.service';

type Stage = 'pickup' | 'dropoff' | 'estimate' | 'requested' | 'active' | 'done';

interface TierOption {
  type: string;
  label: string;
  blurb: string;
  capacity: number;
  estimate?: FareEstimate;
}

const TIERS: TierOption[] = [
  { type: 'MOTO', label: 'Moto', blurb: 'Fastest through traffic', capacity: 1 },
  { type: 'STANDARD', label: 'Standard', blurb: 'Everyday rides', capacity: 4 },
  { type: 'COMFORT', label: 'Comfort', blurb: 'Newer cars, extra legroom', capacity: 4 },
  { type: 'XL', label: 'XL', blurb: 'Up to 6 riders', capacity: 6 },
  { type: 'PREMIUM', label: 'Premium', blurb: 'High-end cars', capacity: 4 },
];

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [MapComponent, TPipe, DecimalPipe],
  template: `
    <app-map #map (tapped)="onTap($event)" />

    @if (stage() === 'pickup' || stage() === 'dropoff') {
      <div class="hint-card">
        <strong>{{ (stage() === 'pickup' ? 'home.pickup' : 'home.dropoff') | t }}</strong>
        <span>{{ 'home.whereTo' | t }}</span>
      </div>
    }

    @if (stage() === 'estimate') {
      <div class="sheet" role="dialog" aria-label="fare options">
        <div class="grabber"></div>
        <div class="route-summary">
          <span>{{ routeKm() | number: '1.1-1' }} km</span>
          <span class="dot">·</span>
          <span>{{ routeMin() | number: '1.0-0' }} min</span>
        </div>

        <div class="promo-row">
          <input #promoInput [placeholder]="'home.promo' | t" [value]="promo()" />
          <button type="button" class="promo-btn" (click)="applyPromo(promoInput.value)">{{ 'home.apply' | t }}</button>
        </div>
        @if (promoMessage()) {
          <div class="promo-msg" [class.bad]="promoBad()">{{ promoMessage() }}</div>
        }

        <div class="tiers">
          @for (tier of tiers(); track tier.type) {
            <button type="button" class="tier" [class.selected]="selectedTier() === tier.type" (click)="selectTier(tier.type)">
              <div class="tier-icon" aria-hidden="true">{{ tierIcon(tier.type) }}</div>
              <div class="tier-meta">
                <strong>{{ tier.label }}</strong>
                <span>{{ tier.blurb }}</span>
              </div>
              <div class="tier-price">
                @if (tier.estimate) {
                  <strong>{{ tier.estimate.estimatedMinFare | number: '1.0-0' }}–{{ tier.estimate.estimatedMaxFare | number: '1.0-0' }}</strong>
                  <span>{{ tier.estimate.currency }}</span>
                } @else {
                  <span class="shimmer"></span>
                }
              </div>
            </button>
          }
        </div>

        <button type="button" class="cta" [disabled]="requesting()" (click)="requestRide()">
          {{ requesting() ? ('common.loading' | t) + '…' : ('home.requestRide' | t) + ' · ' + selectedTierLabel() }}
        </button>
      </div>
    }

    @if (stage() === 'requested') {
      <div class="sheet small">
        <div class="status-pulse"><span class="pulse"></span></div>
        <h2>{{ 'home.findingDriver' | t }}</h2>
        <button type="button" class="ghost danger" (click)="cancelRide()">{{ 'home.cancelRide' | t }}</button>
      </div>
    }

    @if (stage() === 'active') {
      <div class="sheet small">
        <h2>{{ (ride()?.status === 'IN_PROGRESS' ? 'home.onTrip' : 'home.driverOnWay') | t }}</h2>
        @if (etaMin() !== null) {
          <p class="eta">{{ etaMin() | number: '1.0-0' }} {{ 'home.eta' | t }}</p>
        }
        @if (ride()?.status === 'ACCEPTED') {
          <button type="button" class="ghost danger" (click)="cancelRide()">{{ 'home.cancelRide' | t }}</button>
        }
      </div>
    }

    @if (stage() === 'done') {
      <div class="sheet small">
        <h2>{{ 'trip.receipt' | t }}</h2>
        @if (ride()?.fare; as fare) {
          <div class="fare-total">{{ fare.totalAmount | number: '1.2-2' }} {{ fare.currency }}</div>
        }
        <button type="button" class="cta" [disabled]="paying()" (click)="pay()">
          {{ paying() ? ('common.loading' | t) + '…' : 'Pay (test mode)' }}
        </button>
        @if (paid()) {
          <p class="paid-ok">{{ receipt() }}</p>
        }
        <button type="button" class="ghost" (click)="reset()">{{ 'common.back' | t }}</button>
      </div>
    }
  `,
  styles: `
    :host { display: block; position: absolute; inset: 0; }
    .hint-card {
      position: absolute; top: calc(env(safe-area-inset-top, 0px) + 84px); left: 50%; transform: translateX(-50%);
      background: var(--bg-glass); backdrop-filter: blur(16px); border: 1px solid var(--line);
      border-radius: var(--radius-lg); padding: 12px 22px; text-align: center; z-index: 400;
      display: flex; flex-direction: column; gap: 2px; animation: pop var(--dur-med) var(--ease-out);
      box-shadow: var(--shadow-md); white-space: nowrap;
    }
    .hint-card span { color: var(--text-dim); font-size: 13px; }
    .sheet {
      position: absolute; bottom: 0; left: 0; right: 0; z-index: 400;
      background: var(--bg-elev); border: 1px solid var(--line); border-bottom: none;
      border-radius: var(--radius-xl) var(--radius-xl) 0 0;
      padding: 12px 18px calc(env(safe-area-inset-bottom, 0px) + 88px);
      box-shadow: var(--shadow-lg); animation: pop var(--dur-med) var(--ease-out);
      max-height: 62dvh; overflow-y: auto;
    }
    .sheet.small { text-align: center; padding-top: 22px; }
    .grabber { width: 42px; height: 4px; border-radius: 4px; background: var(--line); margin: 0 auto 12px; }
    .route-summary { display: flex; gap: 8px; justify-content: center; color: var(--text-dim); font-size: 13px; margin-bottom: 12px; }
    .promo-row { display: flex; gap: 8px; margin-bottom: 6px; }
    .promo-row input {
      flex: 1; padding: 11px 14px; border-radius: var(--radius-md);
      background: var(--bg); border: 1px solid var(--line); color: var(--text);
      text-transform: uppercase; letter-spacing: 1px; font-size: 13px;
    }
    .promo-row input:focus { outline: none; border-color: var(--brand); }
    .promo-btn {
      padding: 0 18px; border-radius: var(--radius-md); border: 1px solid var(--brand);
      background: var(--brand-soft); color: var(--brand-strong); font-weight: 700;
      transition: transform var(--dur-fast) var(--ease-spring);
    }
    .promo-btn:active { transform: scale(0.95); }
    .promo-msg { font-size: 12.5px; color: var(--accent); margin: 0 2px 8px; }
    .promo-msg.bad { color: var(--danger); }
    .tiers { display: flex; flex-direction: column; gap: 8px; margin-bottom: 14px; }
    .tier {
      display: flex; align-items: center; gap: 14px; text-align: start;
      padding: 12px 14px; border-radius: var(--radius-md);
      background: var(--bg); border: 1.5px solid var(--line); color: var(--text);
      transition: border-color var(--dur-fast), background var(--dur-fast), transform var(--dur-fast) var(--ease-spring);
    }
    .tier:active { transform: scale(0.985); }
    .tier.selected { border-color: var(--brand); background: var(--brand-soft); }
    .tier-icon { font-size: 22px; width: 44px; height: 44px; border-radius: var(--radius-sm); background: var(--bg-card); display: grid; place-items: center; }
    .tier-meta { flex: 1; display: flex; flex-direction: column; }
    .tier-meta span { font-size: 12px; color: var(--text-dim); }
    .tier-price { text-align: end; display: flex; flex-direction: column; }
    .tier-price span { font-size: 11px; color: var(--text-faint); }
    .shimmer { display: block; width: 56px; height: 14px; border-radius: 6px; background: var(--line); animation: fade-in 1s infinite alternate; }
    .cta {
      width: 100%; padding: 15px; border: none; border-radius: var(--radius-md);
      background: linear-gradient(135deg, var(--brand), var(--brand-strong));
      color: #fff; font-weight: 700; font-size: 15px;
      box-shadow: 0 10px 26px rgba(79, 124, 255, 0.35);
      transition: transform var(--dur-fast) var(--ease-spring), filter var(--dur-fast);
    }
    .cta:active:not(:disabled) { transform: scale(0.98); }
    .cta:disabled { opacity: 0.55; }
    .ghost {
      width: 100%; margin-top: 10px; padding: 12px; border-radius: var(--radius-md);
      border: 1px solid var(--line); background: transparent; color: var(--text-dim); font-weight: 600;
    }
    .ghost.danger { color: var(--danger); border-color: var(--danger-soft); background: var(--danger-soft); }
    h2 { margin: 6px 0 10px; font-size: 19px; letter-spacing: -0.01em; }
    .eta { color: var(--text-dim); margin: 0 0 8px; }
    .status-pulse { display: grid; place-items: center; margin-bottom: 6px; }
    .pulse { width: 14px; height: 14px; border-radius: 50%; background: var(--brand); animation: pulse-dot 1.2s infinite; }
    .fare-total { font-size: 34px; font-weight: 800; letter-spacing: -0.02em; margin: 8px 0 14px; }
    .paid-ok { color: var(--accent); font-weight: 700; margin: 10px 0 0; }
  `,
})
export class HomeComponent implements OnDestroy {
  @ViewChild('map') map!: MapComponent;

  private rideService = inject(RideService);
  private api = inject(ApiService);
  private router = inject(Router);

  readonly stage = signal<Stage>('pickup');
  readonly tiers = signal<TierOption[]>(TIERS.map((t) => ({ ...t })));
  readonly selectedTier = signal('STANDARD');
  readonly ride = signal<Ride | null>(null);
  readonly routeKm = signal(0);
  readonly routeMin = signal(0);
  readonly etaMin = signal<number | null>(null);
  readonly promo = signal('');
  readonly promoMessage = signal('');
  readonly promoBad = signal(false);
  readonly requesting = signal(false);
  readonly paying = signal(false);
  readonly paid = signal(false);
  readonly receipt = signal('');

  private pickup: { lat: number; lng: number } | null = null;
  private dropoff: { lat: number; lng: number } | null = null;
  private routeCoords: [number, number][] | null = null;
  private pollSub?: Subscription;

  readonly selectedTierLabel = computed(
    () => this.tiers().find((t) => t.type === this.selectedTier())?.label ?? '',
  );

  tierIcon(type: string): string {
    return { MOTO: '🛵', STANDARD: '🚗', COMFORT: '🚙', XL: '🚐', PREMIUM: '🏎️' }[type] ?? '🚗';
  }

  onTap(point: { lat: number; lng: number }): void {
    if (this.stage() === 'pickup') {
      this.pickup = point;
      this.map.setPickup(point.lat, point.lng);
      this.stage.set('dropoff');
    } else if (this.stage() === 'dropoff') {
      this.dropoff = point;
      this.map.setDropoff(point.lat, point.lng);
      this.loadRouteAndEstimates();
    }
  }

  private loadRouteAndEstimates(): void {
    if (!this.pickup || !this.dropoff) return;
    this.stage.set('estimate');
    const { pickup, dropoff } = this;
    this.rideService.route(pickup.lat, pickup.lng, dropoff.lat, dropoff.lng).subscribe({
      next: (res) => {
        this.routeKm.set(res.route.distanceKm);
        this.routeMin.set(res.route.durationMinutes);
        this.routeCoords = res.route.geometry;
        this.map.drawRoute(res.route.geometry);
        this.map.fitAll();
        this.fetchEstimates();
      },
      error: () => this.fetchEstimates(),
    });
  }

  private fetchEstimates(): void {
    if (!this.pickup || !this.dropoff) return;
    const { pickup, dropoff } = this;
    const promoCode = this.promoBad() || !this.promo() ? undefined : this.promo();
    const calls = this.tiers().map((tier) =>
      this.rideService.estimate({
        pickupLat: pickup.lat, pickupLng: pickup.lng,
        dropoffLat: dropoff.lat, dropoffLng: dropoff.lng,
        vehicleType: tier.type,
        distanceKm: this.routeKm() || undefined,
        durationMin: this.routeMin() || undefined,
        promoCode,
      }),
    );
    forkJoin(calls).subscribe({
      next: (estimates) => {
        this.tiers.update((list) => list.map((t, i) => ({ ...t, estimate: estimates[i] })));
      },
      error: () => {},
    });
  }

  selectTier(type: string): void {
    this.selectedTier.set(type);
  }

  applyPromo(code: string): void {
    const trimmed = code.trim();
    if (!trimmed) {
      this.promo.set('');
      this.promoMessage.set('');
      this.promoBad.set(false);
      this.fetchEstimates();
      return;
    }
    this.rideService.validatePromo(trimmed).subscribe({
      next: (res) => {
        this.promoBad.set(!res.valid);
        this.promoMessage.set(res.valid ? `${res.code} applied` : 'Invalid or expired code');
        this.promo.set(res.valid ? res.code : '');
        this.fetchEstimates();
      },
      error: () => {
        this.promoBad.set(true);
        this.promoMessage.set('Invalid or expired code');
      },
    });
  }

  requestRide(): void {
    if (!this.pickup || !this.dropoff || this.requesting()) return;
    this.requesting.set(true);
    this.rideService
      .request({
        pickupLat: this.pickup.lat, pickupLng: this.pickup.lng,
        dropoffLat: this.dropoff.lat, dropoffLng: this.dropoff.lng,
        vehicleType: this.selectedTier(),
        searchRadiusKm: 10,
      })
      .subscribe({
        next: (ride) => {
          this.requesting.set(false);
          this.ride.set(ride);
          this.stage.set('requested');
          this.startPolling(ride.id);
        },
        error: () => this.requesting.set(false),
      });
  }

  private startPolling(rideId: string): void {
    this.stopPolling();
    this.pollSub = interval(3000)
      .pipe(switchMap(() => this.rideService.get(rideId)))
      .subscribe({
        next: (ride) => {
          const prev = this.ride()?.status;
          this.ride.set(ride);
          if (ride.status === 'ACCEPTED' || ride.status === 'IN_PROGRESS') {
            this.stage.set('active');
            if (ride.driverId) this.trackDriver(ride.driverId);
          } else if (ride.status === 'COMPLETED') {
            this.stage.set('done');
            this.stopPolling();
          } else if (ride.status === 'CANCELLED') {
            this.stopPolling();
            this.reset();
          }
          void prev;
        },
        error: () => {},
      });
  }

  private driverPoll?: Subscription;

  private trackDriver(driverId: string): void {
    if (this.driverPoll) return;
    this.driverPoll = interval(2500)
      .pipe(switchMap(() => this.api.get<{ lat: number; lng: number }>(`/api/location/${driverId}`)))
      .subscribe({
        next: (loc) => {
          this.map.setDriver(loc.lat, loc.lng);
          const target = this.ride()?.status === 'IN_PROGRESS' ? this.dropoff : this.pickup;
          if (target) {
            this.rideService.route(loc.lat, loc.lng, target.lat, target.lng).subscribe({
              next: (res) => this.etaMin.set(res.route.durationMinutes),
              error: () => {},
            });
          }
        },
        error: () => {},
      });
  }

  cancelRide(): void {
    const ride = this.ride();
    if (!ride) return;
    this.rideService.cancel(ride.id, 'Changed my mind').subscribe({
      next: () => this.reset(),
      error: () => this.reset(),
    });
  }

  pay(): void {
    const ride = this.ride();
    if (!ride || this.paying()) return;
    this.paying.set(true);
    this.rideService.myPayments().subscribe({
      next: (page) => {
        const payment = page.content.find((p) => p.rideId === ride.id);
        if (!payment) {
          this.paying.set(false);
          return;
        }
        this.rideService.charge(payment.id).subscribe({
          next: () => this.confirm(payment.id),
          error: () => this.paying.set(false),
        });
      },
      error: () => this.paying.set(false),
    });
  }

  private confirm(paymentId: string): void {
    const ride = this.ride();
    this.rideService.confirmSimulated(paymentId).subscribe({
      next: () => {
        this.paying.set(false);
        this.paid.set(true);
        if (ride) {
          this.rideService.receipt(ride.id).subscribe({
            next: (r) => this.receipt.set(`${r.receiptNumber} · ${r.amount} ${r.currency} · ${r.status}`),
            error: () => {},
          });
        }
      },
      error: () => this.paying.set(false),
    });
  }

  reset(): void {
    this.stopPolling();
    this.ride.set(null);
    this.pickup = null;
    this.dropoff = null;
    this.routeCoords = null;
    this.stage.set('pickup');
    this.etaMin.set(null);
    this.paid.set(false);
    this.receipt.set('');
    this.promo.set('');
    this.promoMessage.set('');
    this.router.navigateByUrl('/');
  }

  private stopPolling(): void {
    this.pollSub?.unsubscribe();
    this.pollSub = undefined;
    this.driverPoll?.unsubscribe();
    this.driverPoll = undefined;
  }

  ngOnDestroy(): void {
    this.stopPolling();
  }
}
