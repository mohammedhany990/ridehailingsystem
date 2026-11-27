import { Injectable, inject, signal } from '@angular/core';
import { ApiService } from './api.service';
import { AppNotification } from './models';
import { Observable, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private api = inject(ApiService);
  readonly unreadCount = signal(0);
  readonly notifications = signal<AppNotification[]>([]);

  refreshUnread(): Observable<number> {
    return this.api.get<number>('/api/notifications/me/unread-count').pipe(
      tap((n) => this.unreadCount.set(n)),
    );
  }

  load(): Observable<AppNotification[]> {
    return this.api.get<AppNotification[]>('/api/notifications/me').pipe(
      tap((list) => this.notifications.set(list)),
    );
  }

  markAllRead(): Observable<unknown> {
    return this.api.put('/api/notifications/me/read-all', {}).pipe(
      tap(() => this.unreadCount.set(0)),
    );
  }
}
