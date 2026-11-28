# JWT Secret Rotation Runbook

All services that validate or sign JWTs share one symmetric secret (HS256), supplied as the `JWT_SECRET` environment variable. auth-service signs; the gateway and every resource server validate with the same value.

## When to rotate

- On any suspected leak (secret in a log, a repo, a screenshot, a chat).
- On a regular cadence (e.g. every 90 days).
- When someone with access leaves the project.

## Impact to expect

Rotation invalidates every outstanding access token and refresh token. Users are not locked out: the next refresh attempt fails and the app routes them to login. Schedule rotations for low-traffic windows.

## Steps (Docker Compose)

1. Generate the new secret: `openssl rand -base64 48`.
2. Update `JWT_SECRET` in `.env` (never commit the value; `.env` is git-ignored).
3. Recreate the affected containers so they pick the value up, validators first order does not matter because all must move together:
   `docker compose up -d --force-recreate api-gateway auth-service user-service ride-service payment-service location-service notification-service pricing-service rating-service map-service`
4. Verify: log in through the gateway (`POST /api/auth/login`), then call an authenticated endpoint (`GET /api/auth/profile/me`) with the new token. A 200 confirms signers and validators agree.
5. Watch for 401 spikes on authenticated traffic in the minutes after rotation; a service that still 401s every valid token is still on the old secret — recreate it.

## CI

The pipeline generates a throwaway secret per run, so no rotation is needed there. Never point CI at a production secret.

## Future hardening (not built yet)

- Asymmetric keys (RS256) so only auth-service can sign.
- Key id (`kid`) headers with overlapping old/new keys for zero-invalidation rotation.
