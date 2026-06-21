package com.ridehailing.rideservice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideRequest {
    @NotNull(message = "Pickup latitude is required")
    @DecimalMin(value = "-90.0",  message = "Invalid pickup latitude")
    @DecimalMax(value = "90.0",   message = "Invalid pickup latitude")
    private Double pickupLat;

    @NotNull(message = "Pickup longitude is required")
    @DecimalMin(value = "-180.0", message = "Invalid pickup longitude")
    @DecimalMax(value = "180.0",  message = "Invalid pickup longitude")
    private Double pickupLng;

    @NotNull(message = "Dropoff latitude is required")
    @DecimalMin(value = "-90.0",  message = "Invalid dropoff latitude")
    @DecimalMax(value = "90.0",   message = "Invalid dropoff latitude")
    private Double dropoffLat;

    @NotNull(message = "Dropoff longitude is required")
    @DecimalMin(value = "-180.0", message = "Invalid dropoff longitude")
    @DecimalMax(value = "180.0",  message = "Invalid dropoff longitude")
    private Double dropoffLng;

    @Positive(message = "Search radius must be positive")
    @DecimalMax(value = "50.0",   message = "Search radius cannot exceed 50 km")
    @Builder.Default
    private Double searchRadiusKm = 5.0;
}
