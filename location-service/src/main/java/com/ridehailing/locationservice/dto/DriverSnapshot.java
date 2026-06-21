package com.ridehailing.locationservice.dto;

import com.ridehailing.locationservice.entity.DriverLocation;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverSnapshot {

    private String driverId;

    private double latitude;
    private double longitude;

    private Double speedKmh;
    private Double bearing;

    private String status;
    private Double rating;

    private Instant updatedAt;


    public static DriverSnapshot fromEntity(DriverLocation entity) {

        return DriverSnapshot.builder()
                .driverId(entity.getDriverId())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .speedKmh(entity.getSpeedKmh())
                .bearing(entity.getBearing())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}