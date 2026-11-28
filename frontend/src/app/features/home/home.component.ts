import { Component, OnDestroy, ViewChild, inject, signal, computed } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { Router } from '@angular/router';
import { forkJoin, Subscription, interval, switchMap } from 'rxjs';
import { MapComponent } from '../../shared/map/map.component';
import { DriverHomeComponent } from '../driver/driver-home.component';
import { ProfileService } from '../../core/profile.service';
import { RideService } from '../../core/ride.service';
import { TPipe } from '../../shared/t.pipe';
import { FareEstimate, FareOffer, Ride } from '../../core/models';
import { StripeSheetComponent } from '../pay/stripe-sheet.component';
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
  imports: [MapComponent, DriverHomeComponent, TPipe, DecimalPipe, StripeSheetComponent],
  template: `
    @if (isDriver()) {
      <app-driver-home />
    } @else {
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

        <div class="offer-row">
          <input #offerInput type="number" inputmode="decimal" min="1" [placeholder]="'home.offerYourFare' | t" [value]="proposedFare() ?? ''" (input)="onProposedInput(offerInput.value)" />
          @if (suggestedFare() !== null) {
            <button type="button" class="chip" (click)="applySuggested(offerInput)">{{ suggestedFare() | number: '1.0-0' }} EGP</button>
          }
        </div>

        <div class="tiers">
          @for (tier of tiers(); track tier.type) {
            <button type="button" class="tier" [class.selected]="selectedTier() === tier.type" (click)="selectTier(tier.type)">
              <div class="tier-icon" [attr.data-tier]="tier.type" aria-hidden="true">
                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  @if (tier.type === 'MOTO') {
                    <circle cx="6" cy="17" r="3"/><circle cx="18" cy="17" r="3"/><path d="M6 17h6l3-7h3M12 7h2l1.5 3"/>
                  } @else if (tier.type === 'XL') {
                    <path d="M3 16V8a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v8M3 16h18M3 16v1a1 1 0 0 0 1 1h1m14-2v1a1 1 0 0 1-1 1h-1"/><circle cx="8" cy="18" r="1.6"/><circle cx="16" cy="18" r="1.6"/><path d="M7 6v4m4-4v4m4-4v4"/>
                  } @else if (tier.type === 'PREMIUM') {
                    <path d="M4 15l1.6-4.6A2 2 0 0 1 7.5 9h9a2 2 0 0 1 1.9 1.4L20 15m-16 0h16v3h-2.5m-11 0H4v-3Zm3 3a1.8 1.8 0 1 0 0-.01Zm10 0a1.8 1.8 0 1 0 0-.01Z"/><path d="M12 5l1.2 1.8L15 6l-.6 1.9"/>
                  } @else if (tier.type === 'COMFORT') {
                    <path d="M4 15l1.6-4.6A2 2 0 0 1 7.5 9h9a2 2 0 0 1 1.9 1.4L20 15m-16 0h16v3h-2.5m-11 0H4v-3Zm3 3a1.8 1.8 0 1 0 0-.01Zm10 0a1.8 1.8 0 1 0 0-.01Z"/><path d="M9 9V7.5A1.5 1.5 0 0 1 10.5 6h3A1.5 1.5 0 0 1 15 7.5V9"/>
                  } @else {
                    <path d="M4 15l1.6-4.6A2 2 0 0 1 7.5 9h9a2 2 0 0 1 1.9 1.4L20 15m-16 0h16v3h-2.5m-11 0H4v-3Zm3 3a1.8 1.8 0 1 0 0-.01Zm10 0a1.8 1.8 0 1 0 0-.01Z"/>
                  }
                </svg>
              </div>
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

        <div class="sheet-footer">
          <button type="button" class="cta" [disabled]="requesting()" (click)="requestRide()">
            {{ requesting() ? ('common.loading' | t) + '…' : ('home.requestRide' | t) + ' · ' + selectedTierLabel() }}
          </button>
        </div>
      </div>
    }

    @if (stage() === 'requested') {
      <div class="sheet small">
        <div class="status-pulse"><span class="pulse"></span></div>
        <h2>{{ 'home.findingDriver' | t }}</h2>
        @if (ride()?.proposedFare; as proposed) {
          <p class="your-offer">{{ 'home.yourOffer' | t }}: {{ proposed | number: '1.0-0' }} EGP</p>
        }
        <div class="offer-row counter-row">
          <input #counterInput type="number" inputmode="decimal" min="1" [placeholder]="'home.counterFare' | t" />
          <button type="button" class="chip" (click)="counterFare(counterInput.value)">{{ 'home.updateFare' | t }}</button>
        </div>
        @if (pendingOffers().length) {
          <div class="offers">
            <p class="offers-title">{{ 'home.driverOffers' | t }}</p>
            @for (offer of pendingOffers(); track offer.id) {
              <div class="offer-card">
                <strong>{{ offer.amount | number: '1.0-0' }} {{ offer.currency }}</strong>
                <div class="offer-actions">
                  <button type="button" class="offer-accept" (click)="acceptOffer(offer.id)">{{ 'home.acceptOffer' | t }}</button>
                  <button type="button" class="offer-decline" (click)="declineOffer(offer.id)">{{ 'home.declineOffer' | t }}</button>
                </div>
              </div>
            }
          </div>
        }
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
        @if (payError()) {
          <p class="pay-error" role="alert">{{ payError() }}</p>
        }
        @if (paid()) {
          <p class="paid-ok">{{ receipt() }}</p>
        }
        <button type="button" class="ghost" (click)="reset()">{{ 'common.back' | t }}</button>
      </div>
    }

    @if (stripeSession(); as s) {
      <app-stripe-sheet
        [clientSecret]="s.clientSecret"
        [publishableKey]="s.publishableKey"
        [paymentId]="s.paymentId"
        [amount]="s.amount"
        [currency]="s.currency"
        (succeeded)="onStripePaid()"
        (failed)="onStripeFailed($event)"
      />
    }
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
      max-height: 68dvh; overflow-y: auto;
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
    .tier-icon { width: 46px; height: 46px; border-radius: var(--radius-sm); background: var(--bg-card); display: grid; place-items: center; color: var(--text-dim); }
    .tier.selected .tier-icon { color: var(--brand-strong); background: var(--bg-elev); }
    .tier-meta { flex: 1; display: flex; flex-direction: column; }
    .tier-meta span { font-size: 12px; color: var(--text-dim); }
    .tier-price { text-align: end; display: flex; flex-direction: column; }
    .tier-price span { font-size: 11px; color: var(--text-faint); }
    .shimmer { display: block; width: 56px; height: 14px; border-radius: 6px; background: var(--line); animation: fade-in 1s infinite alternate; }
    .sheet-footer {
      position: sticky; bottom: calc(-1 * (env(safe-area-inset-bottom, 0px) + 88px));
      margin: 10px -18px calc(-1 * (env(safe-area-inset-bottom, 0px) + 88px));
      padding: 12px 18px calc(env(safe-area-inset-bottom, 0px) + 100px);
      background: linear-gradient(to top, var(--bg-elev) 78%, transparent);
    }
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
    .pay-error { color: #ff6b6b; font-size: 13px; margin-top: 8px; }
    .paid-ok { color: var(--accent); font-weight: 700; margin: 10px 0 0; }
    .offer-row { display: flex; gap: 8px; margin-bottom: 10px; }
    .offer-row input {
      flex: 1; padding: 11px 14px; border-radius: var(--radius-md);
      background: var(--bg); border: 1px solid var(--line); color: var(--text); font-size: 13.5px;
    }
    .offer-row input:focus { outline: none; border-color: var(--brand); }
    .chip {
      padding: 0 16px; border-radius: var(--radius-md); border: 1px solid var(--line);
      background: var(--bg-card); color: var(--text-dim); font-weight: 700; white-space: nowrap;
      transition: transform var(--dur-fast) var(--ease-spring);
    }
    .chip:active { transform: scale(0.95); }
    .your-offer { color: var(--text-dim); font-size: 13px; margin: 0 0 8px; }
    .counter-row { margin-top: 6px; }
    .offers { text-align: start; margin-top: 10px; }
    .offers-title { font-size: 12.5px; color: var(--text-dim); margin: 0 0 6px; }
    .offer-card {
      display: flex; align-items: center; justify-content: space-between; gap: 10px;
      padding: 11px 14px; border-radius: var(--radius-md); margin-bottom: 8px;
      background: var(--brand-soft); border: 1.5px solid var(--brand);
      animation: pop var(--dur-med) var(--ease-out);
    }
    .offer-card strong { font-size: 17px; }
    .offer-actions { display: flex; gap: 8px; }
    .offer-accept {
      padding: 8px 16px; border: none; border-radius: var(--radius-sm);
      background: var(--brand); color: #fff; font-weight: 700;
    }
    .offer-decline {
      padding: 8px 14px; border-radius: var(--radius-sm);
      border: 1px solid var(--line); background: transparent; color: var(--text-dim); font-weight: 600;
    }
  `,
})
export class HomeComponent implements OnDestroy {
  @ViewChild('map') map!: MapComponent;

  private rideService = inject(RideService);
  private profileService = inject(ProfileService);

  readonly isDriver = () => this.profileService.profile()?.role === 'DRIVER';
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
  readonly payError = signal('');
  readonly stripeSession = signal<{ clientSecret: string; publishableKey: string; paymentId: string; amount: number; currency: string } | null>(null);
  readonly paid = signal(false);
  readonly receipt = signal('');
  readonly proposedFare = signal<number | null>(null);
  readonly offers = signal<FareOffer[]>([]);

  readonly pendingOffers = computed(() => this.offers().filter((o) => o.status === 'PENDING'));
  readonly suggestedFare = computed(() => {
    const tier = this.tiers().find((x) => x.type === this.selectedTier());
    if (!tier?.estimate) return null;
    return Math.round((tier.estimate.estimatedMinFare + tier.estimate.estimatedMaxFare) / 2);
  });

  private pickup: { lat: number; lng: number } | null = null;
  private dropoff: { lat: number; lng: number } | null = null;
  private routeCoords: [number, number][] | null = null;
  private pollSub?: Subscription;

  readonly selectedTierLabel = computed(
    () => this.tiers().find((t) => t.type === this.selectedTier())?.label ?? '',
  );


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

  onProposedInput(value: string): void {
    const parsed = Number(value);
    this.proposedFare.set(value && Number.isFinite(parsed) && parsed > 0 ? parsed : null);
  }

  applySuggested(input: HTMLInputElement): void {
    const suggested = this.suggestedFare();
    if (suggested === null) return;
    input.value = String(suggested);
    this.proposedFare.set(suggested);
  }

  counterFare(value: string): void {
    const ride = this.ride();
    const parsed = Number(value);
    if (!ride || !value || !Number.isFinite(parsed) || parsed <= 0) return;
    this.proposedFare.set(parsed);
    this.rideService.updateProposedFare(ride.id, parsed).subscribe({ error: () => {} });
  }

  acceptOffer(offerId: string): void {
    const ride = this.ride();
    if (!ride) return;
    this.rideService.acceptOffer(ride.id, offerId).subscribe({
      next: () => this.refreshOffers(ride.id),
      error: () => {},
    });
  }

  declineOffer(offerId: string): void {
    const ride = this.ride();
    if (!ride) return;
    this.rideService.declineOffer(ride.id, offerId).subscribe({
      next: () => this.refreshOffers(ride.id),
      error: () => {},
    });
  }

  private refreshOffers(rideId: string): void {
    this.rideService.offers(rideId).subscribe({
      next: (offers) => this.offers.set(offers),
      error: () => {},
    });
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
        proposedFare: this.proposedFare() ?? undefined,
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
          if (ride.status === 'REQUESTED') this.refreshOffers(ride.id);
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
          next: (chargeRes) => {
            if (chargeRes.gateway === 'stripe' && chargeRes.clientSecret) {
              this.rideService.paymentConfig().subscribe({
                next: (cfg) => {
                  this.paying.set(false);
                  if (cfg.publishableKey) {
                    this.stripeSession.set({
                      clientSecret: chargeRes.clientSecret!,
                      publishableKey: cfg.publishableKey,
                      paymentId: payment.id,
                      amount: chargeRes.amount,
                      currency: chargeRes.currency,
                    });
                  } else {
                    this.payError.set('Stripe publishable key is not configured');
                  }
                },
                error: () => this.paying.set(false),
              });
            } else {
              this.confirm(payment.id);
            }
          },
          error: () => this.paying.set(false),
        });
      },
      error: () => this.paying.set(false),
    });
  }

  private confirm(paymentId: string): void {
    this.rideService.confirmSimulated(paymentId).subscribe({
      next: () => {
        this.paying.set(false);
        this.showReceipt();
      },
      error: () => this.paying.set(false),
    });
  }

  onStripePaid(): void {
    this.stripeSession.set(null);
    this.showReceipt();
  }

  onStripeFailed(message: string): void {
    this.stripeSession.set(null);
    if (message) this.payError.set(message);
  }

  private showReceipt(): void {
    this.paid.set(true);
    const ride = this.ride();
    if (ride) {
      this.rideService.receipt(ride.id).subscribe({
        next: (r) => this.receipt.set(`${r.receiptNumber} · ${r.amount} ${r.currency} · ${r.status}`),
        error: () => {},
      });
    }
  }

  reset(): void {
    this.stopPolling();
    this.ride.set(null);
    this.pickup = null;
    this.dropoff = null;
    this.routeCoords = null;
    this.stage.set('pickup');
    this.offers.set([]);
    this.proposedFare.set(null);
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
