package com.ridehailing.paymentservice.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RideEvent(
        String eventType,
        UUID rideId,
        UUID riderId,
        UUID driverId,
        String reason,
        Instant occurredAt
) {
}
