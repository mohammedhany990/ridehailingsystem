package com.ridehailing.paymentservice.dto;

import com.ridehailing.paymentservice.entity.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ChargeResponse(
        UUID id,
        UUID rideId,
        BigDecimal amount,
        String currency,
        String status,
        Instant createdAt,
        String gateway,
        String clientSecret
) {
    public static ChargeResponse from(Payment p, String clientSecret, boolean liveGateway) {
        return new ChargeResponse(
                p.getId(), p.getRideId(), p.getAmount(), p.getCurrency(),
                p.getStatus().name(), p.getCreatedAt(),
                liveGateway ? "stripe" : "simulated",
                clientSecret);
    }
}
