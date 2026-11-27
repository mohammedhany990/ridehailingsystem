package com.ridehailing.rideservice.service;


import com.ridehailing.rideservice.client.dto.RouteRequest;
import com.ridehailing.rideservice.client.dto.RouteResponse;
import com.ridehailing.rideservice.common.ApiResponse;
import com.ridehailing.rideservice.dto.*;
import com.ridehailing.rideservice.entity.*;
import com.ridehailing.rideservice.event.RideEvent;
import com.ridehailing.rideservice.kafka.RideEventPublisher;
import com.ridehailing.rideservice.exception.*;
import com.ridehailing.rideservice.mapper.RideMapper;
import com.ridehailing.rideservice.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RideService {
    private final RideRemoteService rideRemoteService;
    private final RideRepository rideRepository;
    private final RideMatchingService rideMatchingService;
    private final FareCalculationService fareCalculationService;
    private final RideMapper rideMapper;
    private final RideEventPublisher rideEventPublisher;

    @Value("${ride.cancellation-fee:10.00}")
    private BigDecimal cancellationFee;

    public ApiResponse<RideResponse> requestRide(UUID riderId, RideRequest request, String bearerToken) {

        UUID driverId = rideMatchingService.findClosestDriver(
                request.getPickupLat(),
                request.getPickupLng(),
                request.getSearchRadiusKm(),
                bearerToken
        );

        Ride ride = Ride.builder()
                .riderId(riderId)
                .driverId(driverId)
                .pickupLat(request.getPickupLat())
                .pickupLng(request.getPickupLng())
                .dropoffLat(request.getDropoffLat())
                .dropoffLng(request.getDropoffLng())
                .status(RideStatus.REQUESTED)
                .vehicleType(request.getVehicleType() != null ? request.getVehicleType() : "STANDARD")
                .requestedAt(Instant.now())
                .build();

        Ride saved = rideRepository.save(ride);

        rideEventPublisher.publish(RideEvent.of(
                "RIDE_REQUESTED", saved.getId(), saved.getRiderId(), saved.getDriverId(), null));

        return ApiResponse.success("Ride requested", rideMapper.toResponse(saved));
    }

    public ApiResponse<RideResponse> acceptRide(UUID rideId, AcceptRideRequest request) {

        Ride ride = findRide(rideId);

        boolean busy = rideRepository.existsByDriverIdAndStatusIn(
                request.getDriverId(),
                List.of(RideStatus.ACCEPTED, RideStatus.IN_PROGRESS)
        );

        if (busy) {
            throw new DriverNotAvailableException("Driver is busy");
        }

        ride.accept(request.getDriverId());

        Ride saved = rideRepository.save(ride);


        rideEventPublisher.publish(RideEvent.of(
                "RIDE_ACCEPTED", saved.getId(), saved.getRiderId(), saved.getDriverId(), null));

        return ApiResponse.success("Ride accepted", rideMapper.toResponse(saved));
    }


    public ApiResponse<RideResponse> startRide(UUID rideId, String bearerToken) {

        Ride ride = findRide(rideId);

        ride.start();

        Ride saved = rideRepository.save(ride);

        rideRemoteService.startDriverRide(
                bearerToken,
                ride.getDriverId()
        );

        rideEventPublisher.publish(RideEvent.of(
                "RIDE_STARTED", saved.getId(), saved.getRiderId(), saved.getDriverId(), null));

        return ApiResponse.success("Ride started", rideMapper.toResponse(saved));
    }

    public ApiResponse<RideResponse> completeRide(UUID rideId, String bearerToken) {

        Ride ride = findRide(rideId);

        ride.complete();

        RouteResponse route = rideRemoteService.getRoute(
                new RouteRequest(
                        ride.getPickupLat(),
                        ride.getPickupLng(),
                        ride.getDropoffLat(),
                        ride.getDropoffLng()
                )
        );
        Fare fare = fareCalculationService.calculate(
                ride,
                route.getRoute().getDistanceKm(),
                route.getRoute().getDurationMinutes()
        );

        ride.setFare(fare);

        Ride saved = rideRepository.save(ride);

        rideRemoteService.finishDriverRide(
                bearerToken,
                ride.getDriverId()
        );
        rideEventPublisher.publish(RideEvent.of(
                "RIDE_COMPLETED", saved.getId(), saved.getRiderId(), saved.getDriverId(), null));

        return ApiResponse.success("Ride completed", rideMapper.toResponse(saved));
    }


    public ApiResponse<RideResponse> cancelRide(UUID rideId, CancelRideRequest request) {

        Ride ride = findRide(rideId);

        ride.cancel(request.getCancelledBy(), request.getReason());

        if (ride.getAcceptedAt() != null && ride.getCancellation() != null) {
            ride.getCancellation().setCancellationFee(cancellationFee);
        }

        Ride saved = rideRepository.save(ride);

        rideEventPublisher.publish(RideEvent.of(
                "RIDE_CANCELLED", saved.getId(), saved.getRiderId(), saved.getDriverId(), request.getReason()));

        return ApiResponse.success("Ride cancelled", rideMapper.toResponse(saved));
    }

    public ApiResponse<RideResponse> getRide(UUID rideId) {

        Ride ride = rideRepository.findByIdWithDetails(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found"));

        return ApiResponse.success("Ride found", rideMapper.toResponse(ride));
    }

    public ApiResponse<List<RideResponse>> getRiderHistory(UUID riderId) {

        List<RideResponse> rides = rideRepository.findByRiderIdOrderByRequestedAtDesc(riderId)
                .stream()
                .map(rideMapper::toResponse)
                .toList();

        return ApiResponse.success("Rider history", rides);
    }

    public ApiResponse<List<RideResponse>> getDriverHistory(UUID driverId) {

        List<RideResponse> rides = rideRepository.findByDriverIdOrderByRequestedAtDesc(driverId)
                .stream()
                .map(rideMapper::toResponse)
                .toList();

        return ApiResponse.success("Driver history", rides);
    }

    private Ride findRide(UUID rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found"));
    }
}