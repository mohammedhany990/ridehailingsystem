package com.ridehailing.locationservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyDriversRequest {

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;


    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    @NotNull(message = "Radius is required")
    @Positive(message = "Radius must be > 0")
    @DecimalMax(value = "50.0", message = "Radius cannot exceed 50 km")
    private Double radiusKm;


    @Positive
    @Max(100)
    @Builder.Default
    private int limit = 20;

    @Builder.Default
    private boolean includeDistance = true;
}
