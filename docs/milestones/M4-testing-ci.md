# M4 — Automated Verification (E2E, Coverage, Gateway Fix)

**Goal:** prove the system works end to end on every push, not just that it compiles.

- `scripts/e2e.sh`: full user journey through the API gateway against the real stack — register → email OTP (read from MailHog) → login → driver goes online and updates location → ride requested/accepted/started/completed → payment auto-created via Kafka → charge + confirm → notifications arrive.
- `docker-compose.e2e.yml` + dedicated CI job that runs the script; it only runs after the compose-smoke job passes.
- **Pre-existing gateway bug fixed:** the gateway stripped the `/api` prefix while controllers expected it, so every request 404'd. Also added the missing `/api/drivers/**` and `/api/vehicles/**` routes. The E2E job is now the regression gate for this.
- JaCoCo coverage reports for notification-service and payment-service, uploaded as CI artifacts.
- Deleted the skeleton `contextLoads` tests (they needed live infrastructure and gave false confidence) and wrote real unit tests instead:
  - `RideStateMachineTest` — ride status transitions (accept/start/complete/cancel rules), 6 tests.
  - `PricingServiceTest` — fare math: base + km + minutes, surge multiplier, minimum-fare floor, ±15% estimate range, defaults, promo discount. 5 tests.
- CI runs `mvn verify` (tests enforced) for notification, payment, ride and pricing services.

**How to verify:** Actions tab — build, compose-smoke and e2e jobs; coverage under job artifacts.
