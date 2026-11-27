package com.ridehailing.rideservice.event;

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
    public static RideEvent of(String eventType, UUID rideId, UUID riderId, UUID driverId, String reason) {
        return new RideEvent(eventType, rideId, riderId, driverId, reason, Instant.now());
    }
}
