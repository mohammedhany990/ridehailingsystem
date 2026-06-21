package com.ridehailing.rideservice.dto;


import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareResponse {
    private UUID        id;
    private BigDecimal  baseFare;
    private BigDecimal  distanceKm;
    private BigDecimal  surgeMultiplier;
    private BigDecimal  totalAmount;
    private String      currency;
    private Instant calculatedAt;
}
