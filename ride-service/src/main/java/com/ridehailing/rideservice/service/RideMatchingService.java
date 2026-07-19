package com.ridehailing.rideservice.service;

import com.ridehailing.rideservice.client.LocationServiceClient;
import com.ridehailing.rideservice.client.dto.LocationApiResponse;
import com.ridehailing.rideservice.client.dto.NearbyDriversRequest;
import com.ridehailing.rideservice.client.dto.NearbyDriversResponse;
import com.ridehailing.rideservice.exception.NoAvailableDriverException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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

    @Retry(name = "location")
    @CircuitBreaker(name = "location", fallbackMethod = "locationFallback")
    @Bulkhead(name = "location")
    public UUID findClosestDriver(
            double pickupLat,
            double pickupLng,
            double radiusKm,
            String bearerToken
    ) {

        if (radiusKm <= 0) {
            throw new IllegalArgumentException("Search radius must be greater than zero.");
        }

        NearbyDriversRequest request = NearbyDriversRequest.builder()
                .latitude(pickupLat)
                .longitude(pickupLng)
                .radiusKm(radiusKm)
                .limit(10)
                .includeDistance(true)
                .build();

        LocationApiResponse<List<NearbyDriversResponse>> response =
                locationServiceClient.getNearbyDrivers(bearerToken, request);

        if (response == null
                || !response.isSuccess()
                || response.getData() == null
                || response.getData().isEmpty()) {

            log.warn(
                    "No available drivers found within {} km of ({}, {})",
                    radiusKm,
                    pickupLat,
                    pickupLng
            );

            throw new NoAvailableDriverException(
                    "No available drivers found in your area. Please try again shortly."
            );
        }

        NearbyDriversResponse closest = response.getData().get(0);

        if (closest.getDriverId() == null || closest.getDriverId().isBlank()) {
            throw new NoAvailableDriverException("Invalid driver returned from Location Service.");
        }

        UUID driverId = UUID.fromString(closest.getDriverId());

        log.info(
                "Matched driver {} at {} km from pickup.",
                driverId,
                closest.getDistanceKm()
        );

        return driverId;
    }

    public UUID locationFallback(
            double pickupLat,
            double pickupLng,
            double radiusKm,
            String bearerToken,
            Exception ex
    ) {
        log.error(
                "Location Service unavailable while searching for nearby drivers. Cause: {}",
                ex.getMessage(),
                ex
        );

        throw new NoAvailableDriverException(
                "Unable to find nearby drivers at the moment. Please try again later."
        );
    }
}