import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { Ride } from './models';

@Injectable({ providedIn: 'root' })
export class DriverService {
  private api = inject(ApiService);

  createProfile(licenseNumber: string): Observable<unknown> {
    return this.api.post('/api/drivers', { licenseNumber });
  }

  myProfile(): Observable<{ id: string; status: string }> {
    return this.api.get('/api/drivers/me');
  }

  goOnline(): Observable<unknown> {
    return this.api.put('/api/drivers/me/online', {});
  }

  goOffline(): Observable<unknown> {
    return this.api.put('/api/drivers/me/offline', {});
  }

  updateLocation(driverId: string, lat: number, lng: number): Observable<unknown> {
    return this.api.post('/api/location/update', { driverId, latitude: lat, longitude: lng });
  }

  rides(driverId: string): Observable<Ride[]> {
    return this.api.get(`/api/rides/driver/${driverId}/history`);
  }

  accept(rideId: string, driverId: string): Observable<Ride> {
    return this.api.put(`/api/rides/${rideId}/accept`, { driverId });
  }

  start(rideId: string): Observable<Ride> {
    return this.api.put(`/api/rides/${rideId}/start`, {});
  }

  complete(rideId: string): Observable<Ride> {
    return this.api.put(`/api/rides/${rideId}/complete`, {});
  }

  postBreadcrumb(rideId: string, lat: number, lng: number): Observable<unknown> {
    return this.api.post(`/api/rides/${rideId}/breadcrumbs`, { lat, lng });
  }

  earnings(): Observable<{ content: { amount: number; currency: string; status: string; createdAt: string }[] }> {
    return this.api.get('/api/payments/driver/me', { page: 0, size: 50 });
  }
}
