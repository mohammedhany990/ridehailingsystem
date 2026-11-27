package com.ridehailing.rideservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProposedFareUpdate {

    @NotNull(message = "Proposed fare is required")
    @DecimalMin(value = "1.0", message = "Proposed fare must be at least 1 EGP")
    private BigDecimal amount;
}
