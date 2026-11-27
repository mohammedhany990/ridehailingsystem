package com.ridehailing.pricingservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FareEstimateResponse {
    private BigDecimal estimatedMinFare;
    private BigDecimal estimatedMaxFare;
    private BigDecimal surgeMultiplier;
    private String currency;
    private String vehicleType;
    private Double distanceKm;
    private Double durationMinutes;
    private String promoCode;
    private java.math.BigDecimal discountAmount;
}
