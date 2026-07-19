package com.ridehailing.rideservice.service;

import com.ridehailing.rideservice.client.MapClient;
import com.ridehailing.rideservice.client.UserServiceClient;
import com.ridehailing.rideservice.client.dto.RouteRequest;
import com.ridehailing.rideservice.client.dto.RouteResponse;
import com.ridehailing.rideservice.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RideRemoteService {

    private final MapClient mapClient;
    private final UserServiceClient userServiceClient;

    @Retry(name = "map")
    @CircuitBreaker(name = "map", fallbackMethod = "routeFallback")
    public RouteResponse getRoute(RouteRequest request) {
        return mapClient.route(request);
    }

    @Retry(name = "user")
    @CircuitBreaker(name = "user", fallbackMethod = "finishRideFallback")
    public void finishDriverRide(String bearerToken, UUID driverId) {
        userServiceClient.finishRide(bearerToken, driverId);
    }
    @Retry(name = "user")
    @CircuitBreaker(name = "user", fallbackMethod = "startRideFallback")
    public void startDriverRide(String bearerToken, UUID driverId) {
        userServiceClient.startRide(bearerToken, driverId);
    }



    public RouteResponse routeFallback(RouteRequest request, Exception ex) {
        log.error("Map Service unavailable: {}", ex.getMessage());
        throw new ServiceUnavailableException(
                "Map service is currently unavailable."
        );
    }

    public void finishRideFallback(String bearerToken,
                                   UUID driverId,
                                   Exception ex) {

        log.error("User Service unavailable: {}", ex.getMessage());

        throw new ServiceUnavailableException(
                "Unable to update driver status."
        );
    }

    public void startRideFallback(
            String bearerToken,
            UUID driverId,
            Exception ex
    ) {
        log.error("User Service unavailable while starting ride: {}", ex.getMessage());

        throw new ServiceUnavailableException(
                "Unable to update driver status. Please try again."
        );
    }

}