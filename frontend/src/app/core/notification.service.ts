import { Injectable, inject, signal } from '@angular/core';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { AppNotification } from './models';
import { Observable, tap } from 'rxjs';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private api = inject(ApiService);
  private auth = inject(AuthService);

  readonly unreadCount = signal(0);
  readonly notifications = signal<AppNotification[]>([]);
  readonly toast = signal<AppNotification | null>(null);

  private stomp?: Client;
  private toastTimer?: ReturnType<typeof setTimeout>;

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

  connect(): void {
    const userId = this.auth.currentUserId();
    const token = this.auth.token();
    if (!userId || this.stomp?.active) return;

    this.stomp = new Client({
      webSocketFactory: () => new SockJS(`${environment.gatewayUrl}/ws/notifications`),
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      reconnectDelay: 5000,
      onConnect: () => {
        this.stomp?.subscribe(`/topic/notifications.${userId}`, (frame) => {
          try {
            const notification = JSON.parse(frame.body) as AppNotification;
            this.notifications.update((list) => [notification, ...list]);
            this.unreadCount.update((n) => n + 1);
            this.showToast(notification);
          } catch {}
        });
      },
    });
    this.stomp.activate();
  }

  disconnect(): void {
    this.stomp?.deactivate();
    this.stomp = undefined;
  }

  private showToast(notification: AppNotification): void {
    this.toast.set(notification);
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toast.set(null), 4500);
  }
}
