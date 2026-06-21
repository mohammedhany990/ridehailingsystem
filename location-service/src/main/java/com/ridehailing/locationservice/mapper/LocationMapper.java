package com.ridehailing.locationservice.mapper;

import com.ridehailing.locationservice.dto.DriverSnapshot;
import com.ridehailing.locationservice.dto.LocationHistoryResponse;
import com.ridehailing.locationservice.dto.LocationResponse;
import com.ridehailing.locationservice.dto.NearbyDriversResponse;
import com.ridehailing.locationservice.entity.DriverLocation;
import com.ridehailing.locationservice.entity.LocationHistory;
import org.springframework.stereotype.Component;
@Component
public class LocationMapper {

    public LocationResponse toResponse(DriverSnapshot d) {
        return LocationResponse.builder()
                .driverId(d.getDriverId())
                .latitude(d.getLatitude())
                .longitude(d.getLongitude())
                .speedKmh(d.getSpeedKmh())
                .bearing(d.getBearing())
                .updatedAt(d.getUpdatedAt())
                .lastHeartbeat(null)
                .build();
    }

    public LocationResponse toResponse(DriverLocation d) {
        return LocationResponse.builder()
                .driverId(d.getDriverId())
                .latitude(d.getLatitude())
                .longitude(d.getLongitude())
                .speedKmh(d.getSpeedKmh())
                .bearing(d.getBearing())
                .updatedAt(d.getUpdatedAt())
                .lastHeartbeat(d.getLastHeartbeat())
                .build();
    }

    public NearbyDriversResponse toNearbyResponse(DriverSnapshot d, Double distanceKm) {
        return NearbyDriversResponse.builder()
                .driverId(d.getDriverId())
                .distanceKm(distanceKm)
                .latitude(d.getLatitude())
                .longitude(d.getLongitude())
                .speedKmh(d.getSpeedKmh())
                .bearing(d.getBearing())
                .build();
    }

    public LocationHistoryResponse toHistoryResponse(LocationHistory h) {
        return LocationHistoryResponse.builder()
                .latitude(h.getLatitude())
                .longitude(h.getLongitude())
                .speedKmh(h.getSpeedKmh())
                .bearing(h.getBearing())
                .createdAt(h.getCreatedAt())
                .build();
    }
}