package com.ridehailing.userservice.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateDriverRequest {

    @NotNull(message = "Driver ID is required")
    private UUID driverId;

    @NotNull(message = "Ride ID is required")
    private UUID rideId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;

    @Size(
            max = 500,
            message = "Comment cannot exceed 500 characters"
    )
    private String comment;
}