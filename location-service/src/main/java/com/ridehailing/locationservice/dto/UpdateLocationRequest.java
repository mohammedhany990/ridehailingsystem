package com.ridehailing.locationservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLocationRequest {

    @NotBlank(message = "driverId is required")
    private String driverId;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0",  message = "Latitude must be <= 90")
    private double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0",  message = "Longitude must be <= 180")
    private double longitude;

    @PositiveOrZero(message = "Speed must be >= 0")
    private Double speedKmh;

    @DecimalMin(value = "0.0",   message = "Bearing must be >= 0")
    @DecimalMax(value = "360.0", message = "Bearing must be <= 360")
    private Double bearing;
}
