package com.ridehailing.pricingservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
public class FareCalculationRequest {

    @NotNull
    private UUID rideId;

    @NotNull
    @Positive
    private Double distanceKm;

    @NotNull
    @Positive
    private Double durationMinutes;

    private String vehicleType = "STANDARD";
}
