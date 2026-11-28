# M11 — Stripe Test-Mode Payments

## What it does

When a trip ends, the rider pays inside the app with a real Stripe payment sheet (card entry, validation, 3-D Secure if the card asks for it) — in **test mode**, so no real money ever moves. Without Stripe keys, the app falls back to a simulated gateway so the whole flow still runs and demos end to end.

## How it works, end to end

- **Trip completed → payment created:** the ride-service publishes a Kafka event; payment-service creates a PENDING payment (idempotent per ride).
- **Charge:** `POST /api/payments/{id}/charge` creates a Stripe Payment Intent with an idempotency key (`ride-<id>`, so retries never double-charge) and returns the intent's **client secret** plus which gateway is active.
- **Card sheet (Angular):** the app loads Stripe.js with the publishable key, mounts a Stripe Payment Element in a bottom sheet styled to the app's dark theme, and confirms the payment client-side. Card data never touches our servers.
- **Final status via webhook:** Stripe calls `POST /api/payments/webhook`. The gateway lets this one path through without a JWT because authentication here is Stripe's **HMAC signature** (`Stripe-Signature` header verified against the webhook signing secret); forged or tampered payloads are rejected with 400. A succeeded intent flips the payment to SUCCEEDED and publishes the payment event (notifications, earnings, receipt).
- **Local dev without a public URL:** webhooks can't reach `localhost`, so `POST /api/payments/{id}/refresh-status` pulls the intent status from Stripe directly (live mode only). The frontend calls it after confirmation.
- **Config discovery:** `GET /api/payments/config` tells the frontend which gateway is live and hands it the publishable key, so one frontend build works in both modes.
- **Fallback:** with `STRIPE_ENABLED=false` or empty keys, the simulated gateway issues fake intent ids and the existing one-tap simulated confirm path is used. CI runs in this mode.

## Configuration (four variables in `.env`, all empty by default)

| Variable | Where it comes from |
| --- | --- |
| `STRIPE_ENABLED` | set to `true` once the keys below are filled |
| `STRIPE_SECRET_KEY` | Stripe Dashboard → Developers → API keys → test **Secret key** (`sk_test_…`) |
| `STRIPE_PUBLISHABLE_KEY` | same page → **Publishable key** (`pk_test_…`); also a frontend Docker build arg |
| `STRIPE_WEBHOOK_SECRET` | Stripe Dashboard → Developers → Webhooks → add endpoint pointing at `/api/payments/webhook` → **Signing secret** (`whsec_…`) |

## Going live — honest note for Egypt

Stripe's official availability list (stripe.com/global) does **not** include Egypt: a live Stripe account cannot be opened with an Egyptian business directly (the documented routes are a supported-country entity, e.g. Stripe Atlas US LLC, or a friend/company abroad). Test mode is unaffected and is what this project integrates and demonstrates.

## Tests

- **Webhook signature tests (3):** a payload signed with a real HMAC-SHA256 `Stripe-Signature` header is accepted and syncs the payment; a forged signature and a tampered payload are both rejected with 400 and never touch the payment.
- **Service tests:** charge returns the client secret; idempotency key is `ride-<id>`; non-chargeable states rejected; webhook success publishes the payment event.
- **CI E2E (real stack):** asserts the config endpoint reports `simulated`, the charge response carries the gateway name, the webhook path passes the gateway without a JWT (and is not 401/403), and the full simulated pay flow still produces a receipt.

## What is not verified

A real charge against Stripe's test API needs real keys, which CI does not have; the Stripe call path (`PaymentIntent.create`, webhook signature verification) is exercised in unit tests with the official `stripe-java` library, and the live click-through needs the owner's test keys.
