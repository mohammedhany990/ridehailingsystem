# M10 — Google Sign-In

## What it does

A rider (or driver) can sign in with their Google account instead of email + password. One click on the Google button, and they are inside the app — registered automatically on first use, logged in on every use after that.

## How it works, end to end

- **Frontend (Angular):** the login and register pages load Google's official sign-in script and render the standard "Continue with Google" button. When the user picks their account, Google returns a signed ID token to the page.
- **API call:** the frontend posts that ID token to `POST /api/auth/google` through the gateway. No password ever exists for these accounts.
- **Backend verification (auth-service):** the server verifies the token itself using Google's official client library — signature against Google's published keys, expiry, issuer, and the audience claim, which must equal the project's own Google client ID. A token minted for a different app is rejected. Verified identity claims used: `sub` (stable Google user id), `email`, `name`.
- **Find-or-create:** if a user with that email exists, their account is linked to Google (`oauthProvider=google`, `oauthId=sub`) and they get the app's own JWT access/refresh pair. If not, an enabled RIDER account is created on the spot and linked.
- **Disabled by default:** with no `GOOGLE_CLIENT_ID` set, the endpoint returns `503` with a clear "not configured" message and the frontend hides the button — the app runs exactly as before.

## Configuration

One variable: `GOOGLE_CLIENT_ID` in `.env`. It is passed to auth-service (server verification) and to the frontend image as a Docker build arg (the button). Setup steps are in the README env sample; the client ID comes from Google Cloud Console (OAuth 2.0 Client ID, type Web, with the app's origin under Authorized JavaScript origins).

## Why this design

- **ID token flow, not a redirect flow:** the SPA stays in control of routing and token storage; the backend stays stateless and only ever sees one verifiable credential.
- **Server-side verification:** the backend never trusts an email claim it did not verify — account takeover via a forged request body is not possible.
- **Account linking by email:** a user who registered with email+password and later clicks Google keeps one account, one ride history.

## Tests

- 5 new unit tests (mocked verifier): new Google user created as enabled RIDER, existing password account linked, returning Google user logs in without writes, invalid token rejected with 401, unconfigured server rejected with 503.
- CI E2E step against the live stack asserts the endpoint exists and behaves correctly in both modes: `503` with a clear message when no client ID is set, `401` for a forged token when one is.

## What is not verified

A real Google-issued token end to end requires a real Google account click-through, which CI cannot do. Signature/audience verification is the official Google library code path; the live click-through was validated by code inspection of the GIS button contract, not by an automated browser session.
