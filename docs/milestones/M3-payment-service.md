# M3 — Payment Service (Stripe Test Mode)

**Goal:** charge the rider when a trip completes, safely and repeatably.

- New `payment-service` (port 8086):
  - Consumes `RIDE_COMPLETED` from Kafka and creates a PENDING payment — idempotently (one payment per ride, enforced by unique constraint + existence check).
  - Stripe integration in **test mode only**: creates a PaymentIntent with an idempotency key, verifies webhook signatures (`STRIPE_WEBHOOK_SECRET`), and never touches real money.
  - **Simulated gateway** (default, `STRIPE_ENABLED=false`): mimics Stripe's flow for local development and CI — charge then confirm — with no external calls.
  - Publishes `PAYMENT_SUCCEEDED` / `PAYMENT_FAILED` events back to Kafka; notification-service turns them into user notifications.
  - Rider payment history and driver earnings history endpoints (paginated, ownership-checked).

**Tests:** 8/8 green (idempotency, ownership checks, simulated gateway, event publishing).
