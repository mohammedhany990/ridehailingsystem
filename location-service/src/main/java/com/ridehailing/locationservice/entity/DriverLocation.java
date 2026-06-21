package com.ridehailing.locationservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "driver_locations")
public class DriverLocation {

    @Id
    private String id;

    private String driverId;

    @GeoSpatialIndexed
    private GeoJsonPoint location;

    private Double speedKmh;
    private Double bearing;

    private Instant updatedAt;
    private Instant lastHeartbeat;

    public static DriverLocation create(String driverId, double lat, double lng) {
        return DriverLocation.builder()
                .id(UUID.randomUUID().toString())
                .driverId(driverId)
                .location(new GeoJsonPoint(lng, lat))
                .updatedAt(Instant.now())
                .lastHeartbeat(Instant.now())
                .build();
    }

    public void updateLocation(double lat, double lng, Double speedKmh, Double bearing) {
        this.location = new GeoJsonPoint(lng, lat);
        this.speedKmh = speedKmh;
        this.bearing = bearing;
        this.updatedAt = Instant.now();
        this.lastHeartbeat = Instant.now();
    }

    public void heartbeat() {
        this.lastHeartbeat = Instant.now();
    }

    public Double getLatitude() {
        return this.location != null ? this.location.getY() : null;
    }

    public Double getLongitude() {
        return this.location != null ? this.location.getX() : null;
    }
}