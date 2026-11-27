package com.ridehailing.notificationservice.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentEvent(
        String eventType,
        UUID paymentId,
        UUID rideId,
        UUID riderId,
        UUID driverId,
        BigDecimal amount,
        String currency,
        String failureReason,
        Instant occurredAt
) {
}
