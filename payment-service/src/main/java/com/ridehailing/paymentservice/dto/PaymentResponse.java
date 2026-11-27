package com.ridehailing.paymentservice.dto;

import com.ridehailing.paymentservice.entity.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID rideId,
        UUID riderId,
        UUID driverId,
        BigDecimal amount,
        String currency,
        String status,
        Instant createdAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getId(), p.getRideId(), p.getRiderId(), p.getDriverId(),
                p.getAmount(), p.getCurrency(), p.getStatus().name(), p.getCreatedAt());
    }
}
