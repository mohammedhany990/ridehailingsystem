import { Component, OnDestroy, OnInit, ViewChild, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subscription, interval, switchMap } from 'rxjs';
import { MapComponent } from '../../shared/map/map.component';
import { DriverService } from '../../core/driver.service';
import { AuthService } from '../../core/auth.service';
import { Ride } from '../../core/models';
import { TPipe } from '../../shared/t.pipe';

type DriverStage = 'setup' | 'offline' | 'online' | 'request' | 'accepted' | 'in_progress' | 'completed';

@Component({
  selector: 'app-driver-home',
  standalone: true,
  imports: [MapComponent, ReactiveFormsModule, DecimalPipe, TPipe],
  template: `
    <app-map #map />

    @if (stage() === 'setup') {
      <div class="sheet">
        <h2>Driver setup</h2>
        <p class="dim">One-time: add your license to start driving.</p>
        <form [formGroup]="setupForm" (ngSubmit)="createProfile()">
          <label for="lic">License number</label>
          <input id="lic" formControlName="licenseNumber" />
          <button class="cta" type="submit" [disabled]="setupForm.invalid">Continue</button>
        </form>
      </div>
    }

    @if (stage() === 'offline') {
      <div class="sheet center">
        <h2>You're offline</h2>
        <p class="dim">Go online to receive ride requests.</p>
        <button class="cta" type="button" (click)="goOnline()">Go online</button>
        @if (todayEarnings() > 0) {
          <p class="earnings">{{ todayEarnings() | number: '1.2-2' }} EGP earned</p>
        }
      </div>
    }

    @if (stage() === 'online') {
      <div class="pill"><span class="pulse"></span> Online · waiting for requests</div>
    }

    @if (stage() === 'request') {
      <div class="sheet center request-card">
        <p class="kicker">New ride request</p>
        <h2>{{ activeRide()?.vehicleType }} ride</h2>
        <p class="dim">Tap accept to take this trip</p>
        <button class="cta" type="button" (click)="accept()">Accept</button>
      </div>
    }

    @if (stage() === 'accepted' || stage() === 'in_progress') {
      <div class="sheet center">
        <h2>{{ stage() === 'accepted' ? 'Head to pickup' : 'Trip in progress' }}</h2>
        @if (stage() === 'accepted') {
          <button class="cta" type="button" (click)="startTrip()">Start trip</button>
        } @else {
          <button class="cta" type="button" (click)="completeTrip()">Complete trip</button>
        }
      </div>
    }

    @if (stage() === 'completed') {
      <div class="sheet center">
        <h2>Trip completed</h2>
        @if (activeRide()?.fare; as fare) {
          <p class="fare">{{ fare.totalAmount | number: '1.2-2' }} {{ fare.currency }}</p>
        }
        <button class="cta" type="button" (click)="backOnline()">Back online</button>
      </div>
    }
  `,
  styles: `
    :host { display: block; position: absolute; inset: 0; }
    .sheet {
      position: absolute; bottom: 0; left: 0; right: 0; z-index: 400;
      background: var(--bg-elev); border: 1px solid var(--line); border-bottom: none;
      border-radius: var(--radius-xl) var(--radius-xl) 0 0;
      padding: 22px 20px calc(env(safe-area-inset-bottom, 0px) + 96px);
      box-shadow: var(--shadow-lg); animation: pop var(--dur-med) var(--ease-out);
    }
    .center { text-align: center; }
    .kicker { color: var(--brand-strong); font-weight: 700; font-size: 12.5px; letter-spacing: 0.08em; text-transform: uppercase; margin: 0 0 4px; }
    h2 { margin: 4px 0 10px; font-size: 19px; }
    .dim { color: var(--text-dim); font-size: 13.5px; margin: 0 0 12px; }
    label { display: block; text-align: start; font-size: 12.5px; font-weight: 600; color: var(--text-dim); margin: 0 0 6px 2px; }
    input {
      width: 100%; padding: 13px 15px; border-radius: var(--radius-md); margin-bottom: 14px;
      background: var(--bg); border: 1px solid var(--line); color: var(--text);
    }
    input:focus { outline: none; border-color: var(--brand); box-shadow: var(--ring); }
    .cta {
      width: 100%; padding: 15px; border: none; border-radius: var(--radius-md);
      background: linear-gradient(135deg, var(--brand), var(--brand-strong));
      color: #fff; font-weight: 700; font-size: 15px;
      box-shadow: 0 10px 26px rgba(79, 124, 255, 0.35);
      transition: transform var(--dur-fast) var(--ease-spring);
    }
    .cta:active:not(:disabled) { transform: scale(0.98); }
    .cta:disabled { opacity: 0.55; }
    .pill {
      position: absolute; top: calc(env(safe-area-inset-top, 0px) + 84px); left: 50%; transform: translateX(-50%);
      background: var(--bg-glass); backdrop-filter: blur(16px); border: 1px solid var(--line);
      border-radius: 30px; padding: 10px 20px; z-index: 400; display: flex; gap: 9px; align-items: center;
      font-size: 13.5px; font-weight: 600; box-shadow: var(--shadow-md); white-space: nowrap;
      animation: pop var(--dur-med) var(--ease-out);
    }
    .pulse { width: 9px; height: 9px; border-radius: 50%; background: var(--accent); animation: pulse-dot 1.4s infinite; }
    .fare { font-size: 32px; font-weight: 800; margin: 4px 0 14px; letter-spacing: -0.02em; }
    .earnings { color: var(--accent); font-weight: 700; margin: 12px 0 0; }
    .request-card { border-color: var(--brand); }
  `,
})
export class DriverHomeComponent implements OnInit, OnDestroy {
  @ViewChild('map') map!: MapComponent;

  private driverService = inject(DriverService);
  private auth = inject(AuthService);
  private fb = inject(FormBuilder);

  readonly stage = signal<DriverStage>('offline');
  readonly activeRide = signal<Ride | null>(null);
  readonly todayEarnings = signal(0);

  setupForm = this.fb.nonNullable.group({
    licenseNumber: ['', [Validators.required, Validators.minLength(4)]],
  });

  private locationSub?: Subscription;
  private pollSub?: Subscription;
  private simLat = 30.0444;
  private simLng = 31.2357;
  private geoWatch?: number;

  ngOnInit(): void {
    this.driverService.myProfile().subscribe({
      next: () => this.stage.set('offline'),
      error: (err) => this.stage.set(err?.status === 404 ? 'setup' : 'offline'),
    });
    this.driverService.earnings().subscribe({
      next: (page) => this.todayEarnings.set(
        page.content.filter((p) => p.status === 'SUCCEEDED').reduce((s, p) => s + p.amount, 0)),
      error: () => {},
    });
  }

  createProfile(): void {
    if (this.setupForm.invalid) return;
    this.driverService.createProfile(this.setupForm.getRawValue().licenseNumber).subscribe({
      next: () => this.stage.set('offline'),
      error: () => {},
    });
  }

  goOnline(): void {
    this.driverService.goOnline().subscribe({
      next: () => {
        this.stage.set('online');
        this.startLocationLoop();
        this.startRequestPolling();
      },
      error: () => {},
    });
  }

  private currentPosition(): { lat: number; lng: number } {
    return { lat: this.simLat, lng: this.simLng };
  }

  private startLocationLoop(): void {
    const driverId = this.auth.currentUserId();
    if (!driverId) return;
    if (navigator.geolocation) {
      this.geoWatch = navigator.geolocation.watchPosition(
        (pos) => {
          this.simLat = pos.coords.latitude;
          this.simLng = pos.coords.longitude;
        },
        () => {},
        { enableHighAccuracy: true },
      );
    }
    this.locationSub = interval(5000).subscribe(() => {
      const ride = this.activeRide();
      if (ride && (ride.status === 'ACCEPTED' || ride.status === 'IN_PROGRESS')) {
        const target = ride.status === 'ACCEPTED'
          ? { lat: ride.pickupLat, lng: ride.pickupLng }
          : { lat: ride.dropoffLat, lng: ride.dropoffLng };
        this.simLat += (target.lat - this.simLat) * 0.18;
        this.simLng += (target.lng - this.simLng) * 0.18;
      }
      const { lat, lng } = this.currentPosition();
      this.driverService.updateLocation(driverId, lat, lng).subscribe({ error: () => {} });
      this.map.setDriver(lat, lng);
      if (ride && (this.stage() === 'accepted' || this.stage() === 'in_progress')) {
        this.driverService.postBreadcrumb(ride.id, lat, lng).subscribe({ error: () => {} });
      }
    });
  }

  private startRequestPolling(): void {
    const driverId = this.auth.currentUserId();
    if (!driverId) return;
    this.pollSub = interval(4000)
      .pipe(switchMap(() => this.driverService.rides(driverId)))
      .subscribe({
        next: (rides) => {
          const active = rides.find((r) => r.status === 'IN_PROGRESS' || r.status === 'ACCEPTED');
          const pending = rides.find((r) => r.status === 'REQUESTED');
          if (active) {
            this.activeRide.set(active);
            this.showRide(active);
            this.stage.set(active.status === 'ACCEPTED' ? 'accepted' : 'in_progress');
          } else if (pending && this.stage() === 'online') {
            this.activeRide.set(pending);
            this.showRide(pending);
            this.stage.set('request');
          }
        },
        error: () => {},
      });
  }

  private showRide(ride: Ride): void {
    this.map.setPickup(ride.pickupLat, ride.pickupLng);
    this.map.setDropoff(ride.dropoffLat, ride.dropoffLng);
    this.map.fitAll();
  }

  accept(): void {
    const ride = this.activeRide();
    const driverId = this.auth.currentUserId();
    if (!ride || !driverId) return;
    this.driverService.accept(ride.id, driverId).subscribe({
      next: (updated) => {
        this.activeRide.set(updated);
        this.stage.set('accepted');
      },
      error: () => {},
    });
  }

  startTrip(): void {
    const ride = this.activeRide();
    if (!ride) return;
    this.driverService.start(ride.id).subscribe({
      next: (updated) => {
        this.activeRide.set(updated);
        this.stage.set('in_progress');
      },
      error: () => {},
    });
  }

  completeTrip(): void {
    const ride = this.activeRide();
    if (!ride) return;
    this.driverService.complete(ride.id).subscribe({
      next: (updated) => {
        this.activeRide.set(updated);
        this.stage.set('completed');
      },
      error: () => {},
    });
  }

  backOnline(): void {
    this.activeRide.set(null);
    this.stage.set('online');
  }

  ngOnDestroy(): void {
    this.locationSub?.unsubscribe();
    this.pollSub?.unsubscribe();
    if (this.geoWatch !== undefined) navigator.geolocation.clearWatch(this.geoWatch);
  }
}
