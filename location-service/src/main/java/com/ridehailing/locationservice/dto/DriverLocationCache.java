package com.ridehailing.locationservice.dto;

import com.ridehailing.locationservice.entity.DriverLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocationCache {

    private String driverId;
    private double latitude;
    private double longitude;
    private Double speedKmh;
    private Double bearing;
    private Instant updatedAt;
    private Instant lastHeartbeat;

    public static DriverLocationCache from(DriverLocation entity) {
        return new DriverLocationCache(
                entity.getDriverId(),
                entity.getLocation() != null ? entity.getLocation().getY() : 0,
                entity.getLocation() != null ? entity.getLocation().getX() : 0,
                entity.getSpeedKmh(),
                entity.getBearing(),
                entity.getUpdatedAt(),
                entity.getLastHeartbeat()
        );
    }

    public DriverLocation toEntity() {
        return DriverLocation.builder()
                .driverId(driverId)
                .location(new GeoJsonPoint(longitude, latitude))
                .speedKmh(speedKmh)
                .bearing(bearing)
                .updatedAt(updatedAt)
                .lastHeartbeat(lastHeartbeat)
                .build();
    }
}