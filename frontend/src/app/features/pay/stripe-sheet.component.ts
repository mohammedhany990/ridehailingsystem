import { Component, ElementRef, EventEmitter, Input, OnDestroy, Output, ViewChild, inject, signal } from '@angular/core';
import { loadStripe, Stripe, StripeElements } from '@stripe/stripe-js';
import { RideService } from '../../core/ride.service';

@Component({
  selector: 'app-stripe-sheet',
  standalone: true,
  template: `
    <div class="pay-sheet">
      <div class="grabber"></div>
      <h2 class="title">Card payment</h2>
      @if (amount) {
        <p class="amount">{{ amount }} <span>{{ currency }}</span></p>
      }
      <div class="card-box" #cardHost></div>
      @if (error()) {
        <p class="pay-error" role="alert">{{ error() }}</p>
      }
      <button type="button" class="cta" [disabled]="!ready() || confirming()" (click)="confirm()">
        {{ confirming() ? 'Processing…' : 'Pay securely' }}
      </button>
      <p class="secure">Test mode · no real money moves · card 4242 4242 4242 4242, any future date, any CVC</p>
      <button type="button" class="ghost" [disabled]="confirming()" (click)="cancel()">Cancel</button>
    </div>
  `,
  styles: `
    .pay-sheet {
      position: absolute; bottom: 0; left: 0; right: 0; z-index: 500;
      background: var(--bg-elev); border: 1px solid var(--line); border-bottom: none;
      border-radius: var(--radius-xl) var(--radius-xl) 0 0;
      padding: 12px 18px calc(env(safe-area-inset-bottom, 0px) + 24px);
      box-shadow: var(--shadow-lg); animation: pop var(--dur-med) var(--ease-out);
    }
    .grabber { width: 42px; height: 4px; border-radius: 4px; background: var(--line); margin: 0 auto 12px; }
    .title { text-align: center; font-size: 17px; margin: 4px 0 2px; }
    .amount { text-align: center; font-size: 30px; font-weight: 800; margin: 0 0 14px; }
    .amount span { font-size: 15px; color: var(--text-dim); font-weight: 600; }
    .card-box {
      background: var(--bg); border: 1px solid var(--line); border-radius: var(--radius-md);
      padding: 14px 12px; margin-bottom: 12px;
    }
    .pay-error { color: #ff6b6b; font-size: 13px; text-align: center; margin: 0 0 10px; }
    .secure { text-align: center; color: var(--text-dim); font-size: 11.5px; margin: 10px 0 6px; }
    .cta {
      width: 100%; padding: 14px; border: none; border-radius: var(--radius-md);
      background: linear-gradient(135deg, var(--brand), var(--brand-strong, var(--brand)));
      color: #fff; font-size: 15px; font-weight: 700; cursor: pointer;
      box-shadow: var(--shadow-md); transition: transform var(--dur-fast) var(--ease-out), filter var(--dur-fast);
    }
    .cta:hover:not(:disabled) { transform: translateY(-1px); filter: brightness(1.06); }
    .cta:disabled { opacity: 0.55; cursor: not-allowed; box-shadow: none; }
    .ghost {
      width: 100%; margin-top: 8px; padding: 12px; background: transparent;
      border: 1px solid var(--line); border-radius: var(--radius-md);
      color: var(--text-dim); font-size: 14px; cursor: pointer;
    }
  `,
})
export class StripeSheetComponent implements OnDestroy {
  @Input({ required: true }) clientSecret!: string;
  @Input({ required: true }) publishableKey!: string;
  @Input({ required: true }) paymentId!: string;
  @Input() amount: number | null = null;
  @Input() currency = '';
  @Output() succeeded = new EventEmitter<void>();
  @Output() failed = new EventEmitter<string>();
  @ViewChild('cardHost') cardHost?: ElementRef<HTMLElement>;

  private rideService = inject(RideService);
  private stripe: Stripe | null = null;
  private elements: StripeElements | null = null;

  readonly ready = signal(false);
  readonly confirming = signal(false);
  readonly error = signal('');

  ngAfterViewInit(): void {
    this.mount();
  }

  private async mount(): Promise<void> {
    try {
      this.stripe = await loadStripe(this.publishableKey);
      if (!this.stripe || !this.cardHost) {
        this.error.set('Stripe failed to load');
        return;
      }
      this.elements = this.stripe.elements({
        clientSecret: this.clientSecret,
        appearance: {
          theme: 'night',
          variables: {
            colorPrimary: '#6c8cff',
            borderRadius: '10px',
          },
        },
      });
      const paymentElement = this.elements.create('payment', { layout: 'tabs' });
      paymentElement.mount(this.cardHost.nativeElement);
      paymentElement.on('ready', () => this.ready.set(true));
    } catch {
      this.error.set('Stripe failed to load');
    }
  }

  async confirm(): Promise<void> {
    if (!this.stripe || !this.elements || this.confirming()) return;
    this.confirming.set(true);
    this.error.set('');
    try {
      const result = await this.stripe.confirmPayment({
        elements: this.elements,
        redirect: 'if_required',
      });
      if (result.error) {
        this.error.set(result.error.message ?? 'Payment failed');
        this.confirming.set(false);
        return;
      }
      this.syncBackend(0);
    } catch {
      this.error.set('Payment failed');
      this.confirming.set(false);
    }
  }

  private syncBackend(attempt: number): void {
    this.rideService.refreshPaymentStatus(this.paymentId).subscribe({
      next: (payment) => {
        if (payment.status === 'SUCCEEDED') {
          this.succeeded.emit();
        } else if (attempt < 4) {
          setTimeout(() => this.syncBackend(attempt + 1), 1500);
        } else {
          this.failed.emit('Payment is still processing; check your receipts in a moment');
        }
      },
      error: () => {
        if (attempt < 4) {
          setTimeout(() => this.syncBackend(attempt + 1), 1500);
        } else {
          this.failed.emit('Could not confirm the payment status');
        }
      },
    });
  }

  cancel(): void {
    this.failed.emit('');
  }

  ngOnDestroy(): void {
    this.elements = null;
    this.stripe = null;
  }
}
