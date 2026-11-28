import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';

export interface DailyEarning {
  driverId: string;
  earningDate: string;
  totalAmount: number;
  rideCount: number;
  currency: string;
}

@Injectable({ providedIn: 'root' })
export class EarningsService {
  private api = inject(ApiService);

  daily(): Observable<DailyEarning[]> {
    return this.api.get('/api/payments/earnings/me/daily');
  }
}
