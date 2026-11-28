import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize(config: { client_id: string; callback: (response: { credential: string }) => void }): void;
          renderButton(element: HTMLElement, options: Record<string, unknown>): void;
        };
      };
    };
  }
}

const GIS_SCRIPT_SRC = 'https://accounts.google.com/gsi/client';

@Injectable({ providedIn: 'root' })
export class GoogleSignInService {
  private scriptPromise: Promise<void> | null = null;
  private onCredential: ((idToken: string) => void) | null = null;

  readonly clientId = environment.googleClientId === '__GOOGLE_CLIENT_ID__' ? '' : environment.googleClientId;

  get enabled(): boolean {
    return this.clientId.length > 0;
  }

  renderButton(element: HTMLElement, onCredential: (idToken: string) => void, onError: (message: string) => void): void {
    this.onCredential = onCredential;
    this.loadScript()
      .then(() => {
        if (!window.google) {
          onError('Google sign-in failed to load');
          return;
        }
        window.google.accounts.id.initialize({
          client_id: this.clientId,
          callback: (response) => {
            if (response?.credential && this.onCredential) {
              this.onCredential(response.credential);
            }
          },
        });
        element.innerHTML = '';
        window.google.accounts.id.renderButton(element, {
          type: 'standard',
          theme: 'outline',
          size: 'large',
          width: 320,
          text: 'continue_with',
          shape: 'pill',
          logo_alignment: 'left',
        });
      })
      .catch(() => onError('Google sign-in failed to load'));
  }

  private loadScript(): Promise<void> {
    if (window.google?.accounts?.id) return Promise.resolve();
    if (this.scriptPromise) return this.scriptPromise;
    this.scriptPromise = new Promise<void>((resolve, reject) => {
      const script = document.createElement('script');
      script.src = GIS_SCRIPT_SRC;
      script.async = true;
      script.defer = true;
      script.onload = () => resolve();
      script.onerror = () => {
        this.scriptPromise = null;
        reject(new Error('script load failed'));
      };
      document.head.appendChild(script);
    });
    return this.scriptPromise;
  }
}
