# M8 — Driver Earnings + Admin Stats

## Driver earnings

- **Daily rollup table:** the M6b rollup job writes one row per driver per day (total earned, trip count), so the earnings screen is a cheap indexed read, not a payment scan.
- **Endpoint:** `GET /api/payments/earnings/me/daily` returns the authenticated driver's daily earnings, newest first. The driver id comes from the JWT `userId` claim.
- **Frontend:** earnings page with a CSS bar chart of recent days, total and trip counts, wired into driver mode.

## Admin stats

Role-gated (`ROLE_ADMIN`) read-only stats across three services:

- `GET /api/auth/admin/stats` — user counts by role (riders, drivers, admins).
- `GET /api/rides/admin/stats` — ride counts by status, today's rides, currently active rides.
- `GET /api/payments/admin/stats` — revenue sums and payment counts.
- **Frontend:** admin dashboard page rendering all three, visible in the shell only when the signed-in user has the admin role.

**Bug the E2E caught here:** the earnings endpoint originally read the driver id from the JWT *subject* (which is the user's email) instead of the `userId` claim, so it 500'd in the live stack — unit tests with a mocked principal had passed. Fixed, with a regression test that uses a real `Jwt` object whose subject is an email.

**Tests:** admin stats controller tests on all three services, earnings endpoint regression test, and the CI E2E asserts the driver earnings endpoint returns real data after a completed paid trip.
