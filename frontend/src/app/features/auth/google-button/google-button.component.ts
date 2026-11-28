import { Component, ElementRef, EventEmitter, OnDestroy, Output, ViewChild, inject } from '@angular/core';
import { GoogleSignInService } from '../../../core/google/google-signin.service';

@Component({
  selector: 'app-google-button',
  standalone: true,
  template: `
    @if (google.enabled) {
      <div class="google-divider"><span>or</span></div>
      <div class="google-host" #host></div>
    }
  `,
  styles: `
    .google-divider {
      display: flex;
      align-items: center;
      gap: 12px;
      margin: 18px 0 14px;
      color: var(--text-dim);
      font-size: 12.5px;
    }
    .google-divider::before,
    .google-divider::after {
      content: '';
      flex: 1;
      height: 1px;
      background: var(--border, rgba(128, 128, 128, 0.3));
    }
    .google-host {
      display: flex;
      justify-content: center;
      min-height: 44px;
    }
  `,
})
export class GoogleButtonComponent implements OnDestroy {
  @ViewChild('host') host?: ElementRef<HTMLElement>;
  @Output() credential = new EventEmitter<string>();
  @Output() failed = new EventEmitter<string>();

  readonly google = inject(GoogleSignInService);
  private rendered = false;

  ngAfterViewInit(): void {
    if (this.rendered || !this.google.enabled || !this.host) return;
    this.rendered = true;
    this.google.renderButton(
      this.host.nativeElement,
      (idToken) => this.credential.emit(idToken),
      (message) => this.failed.emit(message),
    );
  }

  ngOnDestroy(): void {
    this.rendered = false;
  }
}
