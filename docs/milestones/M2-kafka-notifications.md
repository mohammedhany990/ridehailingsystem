# M2 — Kafka Event Streaming + Notification Service

**Goal:** real asynchronous events between services, and a notification service that reacts to them.

- Apache Kafka (KRaft mode, no ZooKeeper) added to Docker Compose with health check and volume.
- New `notification-service` (port 8090):
  - Consumes ride events from Kafka and stores notifications per user.
  - REST API: list my notifications, unread count, mark one/all read.
  - Live push over WebSocket (STOMP at `/ws/notifications`).
  - Channel-based design: a `NotificationChannel` interface per delivery channel, so email/push channels plug in later without touching core logic.
- `ride-service` publishes an event on every ride transition: REQUESTED, ACCEPTED, STARTED, COMPLETED, CANCELLED.
- Removed the old synchronous Feign notification client — events replace direct coupling.
- Dependabot enabled for dependency updates.

**Tests:** 5/5 unit tests green (service logic + Kafka listener with embedded broker).
