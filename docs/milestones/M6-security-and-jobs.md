# M6 — Security Hardening + Background Jobs

## Security (M6)

- **Gateway-edge JWT validation:** the API gateway is now a real OAuth2 resource server. Every non-public request must carry a valid HS256 JWT issued by auth-service; invalid or missing tokens get 401 at the edge before touching any service. Public paths are exactly: register, login, verify-email, resend-verification-otp, forgot/reset password, actuator health, WebSocket handshake.
- **Security headers on every response:** X-Content-Type-Options, X-Frame-Options DENY, Referrer-Policy no-referrer, X-XSS-Protection, applied as gateway default filters.
- **Per-route Redis rate limiting:** IP-keyed on the auth route (10 rps replenish, burst 20) and user-keyed on business routes (20 rps / burst 40, location 60/120). Real 429s protect login and OTP endpoints from brute force.
- **Role-based authorization:** the JWT `role` claim maps to `ROLE_*` authorities in the ride and payment resource servers, and `/api/*/admin/**` requires `ROLE_ADMIN`.
- **CSRF correctly scoped:** disabled for the stateless token API (it protects cookie-based sessions; with Bearer tokens it only broke legitimate POSTs).

## Background jobs (M6b, ShedLock)

Scheduled jobs now run under **ShedLock**, so with multiple replicas of a service only one instance runs each job at a time (the lock lives in Postgres / MongoDB, auto-created at startup).

- **Stale ride expiry (ride-service):** rides stuck in REQUESTED beyond the TTL are expired automatically so they never hang forever.
- **Daily earnings rollup (payment-service):** aggregates completed payments into a per-driver-per-day earnings table; the driver earnings page reads this instead of scanning payments.
- **Location history cleanup (location-service):** prunes old breadcrumb/location history so the geo store stays small.
- **Stale driver sweep (location-service):** drivers whose location heartbeat stopped are marked offline.

**Tests:** job logic unit tests (stale-ride expiry, earnings rollup, cleanup), gateway security config tests, and the CI E2E continues to pass through the secured gateway on every merge.
