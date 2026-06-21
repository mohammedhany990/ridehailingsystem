package com.ridehailing.rideservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptRideRequest {

    @NotNull(message = "Driver ID is required")
    private UUID driverId;
}
