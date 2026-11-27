package com.ridehailing.paymentservice.dto;

import com.ridehailing.paymentservice.entity.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReceiptResponse(
        String receiptNumber,
        UUID paymentId,
        UUID rideId,
        BigDecimal amount,
        String currency,
        String status,
        String paymentMethod,
        Instant issuedAt
) {
    public static ReceiptResponse from(Payment p) {
        boolean simulated = p.getStripePaymentIntentId() != null
                && p.getStripePaymentIntentId().startsWith("sim_");
        return new ReceiptResponse(
                "RCT-" + p.getId().toString().substring(0, 8).toUpperCase(),
                p.getId(),
                p.getRideId(),
                p.getAmount(),
                p.getCurrency(),
                p.getStatus().name(),
                simulated ? "SIMULATED_CARD" : "CARD",
                p.getUpdatedAt());
    }
}
