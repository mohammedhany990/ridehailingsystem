package com.ridehailing.rideservice.service;


import com.ridehailing.rideservice.client.LocationServiceClient;
import com.ridehailing.rideservice.client.dto.LocationApiResponse;
import com.ridehailing.rideservice.client.dto.NearbyDriversRequest;
import com.ridehailing.rideservice.client.dto.NearbyDriversResponse;
import com.ridehailing.rideservice.exception.NoAvailableDriverException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideMatchingService {

    private final LocationServiceClient locationServiceClient;


    public UUID findClosestDriver(double pickupLat, double pickupLng,
                                  double radiusKm, String bearerToken) {

        NearbyDriversRequest request = NearbyDriversRequest.builder()
                .latitude(pickupLat)
                .longitude(pickupLng)
                .radiusKm(radiusKm)
                .limit(10)
                .includeDistance(true)
                .build();

        LocationApiResponse<List<NearbyDriversResponse>> response =
                locationServiceClient.getNearbyDrivers(bearerToken, request);

        if (response == null || !response.isSuccess()
                || response.getData() == null || response.getData().isEmpty()) {
            log.warn("No available drivers found within {} km of ({}, {})", radiusKm, pickupLat, pickupLng);
            throw new NoAvailableDriverException("No available drivers found in your area. Please try again shortly.");
        }

        // location-service already sorts by distance asc, pick first
        NearbyDriversResponse closest = response.getData().get(0);
        log.info("Matched driverId={} at {}km from pickup", closest.getDriverId(), closest.getDistanceKm());
        return UUID.fromString(closest.getDriverId());
    }
}

