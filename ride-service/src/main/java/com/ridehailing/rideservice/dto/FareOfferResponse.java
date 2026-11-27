package com.ridehailing.rideservice.dto;

import com.ridehailing.rideservice.entity.FareOffer;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareOfferResponse {

    private UUID id;
    private UUID rideId;
    private UUID driverId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private Instant createdAt;

    public static FareOfferResponse from(FareOffer offer) {
        return FareOfferResponse.builder()
                .id(offer.getId())
                .rideId(offer.getRide().getId())
                .driverId(offer.getDriverId())
                .amount(offer.getAmount())
                .currency(offer.getCurrency())
                .status(offer.getStatus().name())
                .createdAt(offer.getCreatedAt())
                .build();
    }
}
