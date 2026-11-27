package com.ridehailing.pricingservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class FareEstimateRequest {

    @NotNull
    private Double pickupLat;

    @NotNull
    private Double pickupLng;

    @NotNull
    private Double dropoffLat;

    @NotNull
    private Double dropoffLng;

    @Positive
    private Double distanceKm;   // pre-computed distance from map-service (optional)

    @Positive
    private Double durationMin;  // pre-computed duration from map-service (optional)

    private String vehicleType = "STANDARD";

    private String promoCode;
}
