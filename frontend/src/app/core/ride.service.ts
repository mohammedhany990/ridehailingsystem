import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { FareEstimate, FareOffer, Receipt, Ride, RouteInfo, Trail } from './models';

export interface RideRequestBody {
  pickupLat: number;
  pickupLng: number;
  dropoffLat: number;
  dropoffLng: number;
  vehicleType: string;
  searchRadiusKm?: number;
  proposedFare?: number;
}

@Injectable({ providedIn: 'root' })
export class RideService {
  private api = inject(ApiService);

  estimate(body: {
    pickupLat: number; pickupLng: number; dropoffLat: number; dropoffLng: number;
    vehicleType: string; distanceKm?: number; durationMin?: number; promoCode?: string;
  }): Observable<FareEstimate> {
    return this.api.post('/api/pricing/estimate', body);
  }

  validatePromo(code: string): Observable<{ code: string; valid: boolean; discountType: string; discountValue: number }> {
    return this.api.post('/api/pricing/promo/validate', { code });
  }

  route(pickupLat: number, pickupLng: number, dropoffLat: number, dropoffLng: number): Observable<{ route: RouteInfo }> {
    return this.api.post('/api/maps/route', { pickupLat, pickupLng, dropoffLat, dropoffLng });
  }

  request(body: RideRequestBody): Observable<Ride> {
    return this.api.post('/api/rides/request', body);
  }

  cancel(rideId: string, reason: string): Observable<Ride> {
    return this.api.put(`/api/rides/${rideId}/cancel`, { cancelledBy: 'RIDER', reason });
  }

  get(rideId: string): Observable<Ride> {
    return this.api.get(`/api/rides/${rideId}`);
  }

  riderHistory(): Observable<Ride[]> {
    return this.api.get('/api/rides/rider/history');
  }

  trail(rideId: string): Observable<Trail> {
    return this.api.get(`/api/rides/${rideId}/trail`);
  }

  offers(rideId: string): Observable<FareOffer[]> {
    return this.api.get(`/api/rides/${rideId}/offers`);
  }

  acceptOffer(rideId: string, offerId: string): Observable<FareOffer> {
    return this.api.put(`/api/rides/${rideId}/offers/${offerId}/accept`, {});
  }

  declineOffer(rideId: string, offerId: string): Observable<FareOffer> {
    return this.api.put(`/api/rides/${rideId}/offers/${offerId}/decline`, {});
  }

  updateProposedFare(rideId: string, amount: number): Observable<unknown> {
    return this.api.put(`/api/rides/${rideId}/proposed-fare`, { amount });
  }

  receipt(rideId: string): Observable<Receipt> {
    return this.api.get(`/api/payments/ride/${rideId}/receipt`);
  }

  myPayments(): Observable<{ content: { id: string; rideId: string }[] }> {
    return this.api.get('/api/payments/me', { page: 0, size: 50 });
  }

  charge(paymentId: string): Observable<unknown> {
    return this.api.post(`/api/payments/${paymentId}/charge`, {});
  }

  confirmSimulated(paymentId: string): Observable<unknown> {
    return this.api.post(`/api/payments/${paymentId}/confirm-simulated`, {});
  }
}
