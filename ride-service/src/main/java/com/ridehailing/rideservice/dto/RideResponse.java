package com.ridehailing.rideservice.dto;

import com.ridehailing.rideservice.entity.RideStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideResponse {
    private UUID                 id;
    private UUID                 riderId;
    private UUID                 driverId;
    private RideStatus           status;
    private String vehicleType;

    private java.math.BigDecimal proposedFare;

    private java.math.BigDecimal negotiatedAmount;
    private Double               pickupLat;
    private Double               pickupLng;
    private Double               dropoffLat;
    private Double               dropoffLng;
    private Instant        requestedAt;
    private Instant        acceptedAt;
    private Instant        startedAt;
    private Instant        completedAt;
    private FareResponse         fare;
    private CancellationResponse cancellation;
}
