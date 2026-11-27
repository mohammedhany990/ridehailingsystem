import { Injectable, inject, signal } from '@angular/core';
import { ApiService } from './api.service';
import { Observable, tap } from 'rxjs';
import { Profile } from './models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private api = inject(ApiService);
  readonly profile = signal<Profile | null>(null);

  load(): Observable<Profile> {
    return this.api.get<Profile>('/api/auth/profile/me').pipe(tap((p) => this.profile.set(p)));
  }

  update(name: string, phone: string): Observable<Profile> {
    return this.api.put<Profile>('/api/auth/profile/me', { name, phone }).pipe(
      tap((p) => this.profile.set(p)),
    );
  }

  uploadAvatar(file: File): Observable<Profile> {
    const form = new FormData();
    form.append('file', file);
    return this.api.postForm<Profile>('/api/auth/profile/me/avatar', form).pipe(
      tap((p) => this.profile.set(p)),
    );
  }

  avatarUrl(userId: string): string {
    return `${environment.gatewayUrl}/api/auth/profile/avatar/${userId}`;
  }
}
