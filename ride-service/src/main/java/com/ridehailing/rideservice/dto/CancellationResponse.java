package com.ridehailing.rideservice.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancellationResponse {
    private UUID          id;
    private String        cancelledBy;
    private String        reason;
    private Instant cancelledAt;
}

