# M5 — Product Features (Backend + Angular Frontend)

## Backend (M5a)

- **Trip breadcrumbs + trail:** the driver app drops GPS points during a ride (`POST /api/rides/{id}/breadcrumbs`); anyone on the trip reads the ordered trail (`GET /api/rides/{id}/trail`). This powers live trip tracking and post-trip replay.
- **Route geometry:** map-service now asks OSRM for the full route polyline and returns it, so the UI draws the real driving route, not a straight line.
- **Ride tiers:** fare rules for STANDARD, PREMIUM, MOTO, COMFORT and XL; the rider picks a tier per request and fare math uses that tier (previously hardcoded "STANDARD").
- **Promo codes:** validate endpoint + automatic discount in estimates (PERCENT or FLAT); `RIDE20` and `WELCOME10` are seeded for demos.
- **Cancellation fee:** cancelling after a driver accepted records a configurable fee (default 10 EGP) on the cancellation.
- **Trip receipt:** `GET /api/payments/ride/{id}/receipt` returns a receipt number, amount, status and payment method — ownership-checked.
- **Gateway gaps fixed:** pricing-service and map-service had no gateway routes at all.

## User profile (M5c)

- View/edit name and phone (`/api/auth/profile/me`).
- Avatar upload with real safety checks: JPG/PNG/WEBP only, 5 MB max, **magic-byte sniffing** (file content is verified, not just the extension), stored outside the repo on a Docker volume, served through an authenticated endpoint.

## Angular frontend (M5b)

- Angular 20 (standalone components + signals), mobile-first "Ryde" app.
- Design system: CSS custom-property tokens, dark and light themes, English + Arabic with full RTL mirroring, reduced-motion support, focus rings.
- Map experience (Leaflet + OpenStreetMap, no paid keys): tap-to-set pickup/dropoff, real OSRM route drawing, theme-matched map tiles, live driver tracking with ETA, animated trip trail replay with scrubber.
- Fare estimate sheet with per-tier pricing and promo codes; ride request/cancel; test-mode payment and receipt.
- Trip history, profile page with avatar upload, live notification plumbing.
- Verified in a real browser (Playwright): every page screenshotted and inspected, zero console errors.

**E2E coverage:** the CI E2E script exercises promo validation, tiered estimate, breadcrumbs + trail, receipt, profile read/update, avatar upload and rejection of a disguised non-image file.
