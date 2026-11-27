package com.ridehailing.rideservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareOfferRequest {

    @NotNull(message = "Driver id is required")
    private UUID driverId;

    @NotNull(message = "Offer amount is required")
    @DecimalMin(value = "1.0", message = "Offer amount must be at least 1 EGP")
    private BigDecimal amount;
}
