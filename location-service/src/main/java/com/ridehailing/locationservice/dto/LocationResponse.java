package com.ridehailing.locationservice.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
public class LocationResponse {

    private String driverId;
    private double latitude;
    private double longitude;
    private Double speedKmh;
    private Double bearing;
    private Instant updatedAt;
    private Instant lastHeartbeat;
}
