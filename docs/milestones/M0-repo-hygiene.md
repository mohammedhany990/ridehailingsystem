# M0 — Repository Hygiene

**Goal:** make the repository safe and reviewable before new work.

- Removed the committed JWT secret from source and moved all secrets to environment variables with a `.env.example` template.
- Added a root `.gitignore` covering build outputs, IDE files and local env files.
- Added the CI workflow (GitHub Actions): per-service Maven build matrix, Docker Compose smoke test that boots the whole stack and polls every health endpoint.
- README refreshed with architecture overview, prerequisites and run instructions.

**How to verify:** push to `main` — the Actions tab shows the build matrix and compose-smoke jobs.

**Note for the owner:** because the JWT secret was once committed, rotate it anywhere it was reused.
