package com.ridehailing.rideservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingCalculationRequest {
    private UUID rideId;
    private Double distanceKm;
    private Double durationMinutes;
    @Builder.Default
    private String vehicleType = "STANDARD";
}
