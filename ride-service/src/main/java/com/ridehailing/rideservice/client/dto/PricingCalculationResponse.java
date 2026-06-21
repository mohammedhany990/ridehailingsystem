package com.ridehailing.rideservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingCalculationResponse {
    private UUID id;
    private UUID rideId;
    private String vehicleType;
    private BigDecimal baseFare;
    private BigDecimal distanceKm;
    private BigDecimal durationMinutes;
    private BigDecimal surgeMultiplier;
    private BigDecimal distanceCost;
    private BigDecimal timeCost;
    private BigDecimal totalAmount;
    private String currency;
    private Instant calculatedAt;
}
