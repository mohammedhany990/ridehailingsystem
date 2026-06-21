package com.ridehailing.rideservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelRideRequest {

    @NotBlank(message = "cancelledBy is required")
    @Pattern(regexp = "RIDER|DRIVER", message = "cancelledBy must be RIDER or DRIVER")
    private String cancelledBy;

    private String reason;
}
