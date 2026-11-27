# M1 — Centralized Configuration (Spring Cloud Config Server)

**Goal:** one place for all service configuration instead of eleven copies.

- New `config-server` module serves configuration from `config-repo/` (plain YAML files in the same Git repo).
- Every service now imports its config from the config server at startup; each local `application.yml` shrank to a few lines.
- Secrets stay outside the repo: the config files reference environment variables (`${JWT_SECRET}`, `${POSTGRES_PASSWORD}`, ...), so no credential ever lives in Git.
- Config server added to Docker Compose with a health check; other services wait for it.
- CI fix included here: the smoke job now generates a real random `JWT_SECRET` (the original wrote the shell command literally into `.env`, crashing every JWT-secured service), and dumps container logs when the stack fails to start.

**How to verify:** with the stack running, `curl http://localhost:8888/actuator/health` (gateway) returns UP, and `curl http://localhost:8888/config/ride-service/default` shows served config.
