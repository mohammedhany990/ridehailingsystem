import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';

export interface UserStats {
  totalUsers: number;
  riders: number;
  drivers: number;
  admins: number;
  activeUsers: number;
}

export interface RideStats {
  totalRides: number;
  ridesToday: number;
  activeRides: number;
  ridesByStatus: Record<string, number>;
}

export interface PaymentStats {
  totalPayments: number;
  succeededPayments: number;
  failedPayments: number;
  totalRevenue: number;
  revenueToday: number;
  currency: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private api = inject(ApiService);

  userStats(): Observable<UserStats> {
    return this.api.get('/api/auth/admin/stats');
  }

  rideStats(): Observable<RideStats> {
    return this.api.get('/api/rides/admin/stats');
  }

  paymentStats(): Observable<PaymentStats> {
    return this.api.get('/api/payments/admin/stats');
  }
}
