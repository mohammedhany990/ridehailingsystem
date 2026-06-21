package com.ridehailing.pricingservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class FareCalculationResponse {
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
    private LocalDateTime calculatedAt;
}
