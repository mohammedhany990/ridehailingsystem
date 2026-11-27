package com.ridehailing.rideservice.service;

import com.ridehailing.rideservice.common.ApiResponse;
import com.ridehailing.rideservice.dto.BreadcrumbPoint;
import com.ridehailing.rideservice.dto.BreadcrumbRequest;
import com.ridehailing.rideservice.dto.TrailResponse;
import com.ridehailing.rideservice.entity.Ride;
import com.ridehailing.rideservice.entity.RideStatus;
import com.ridehailing.rideservice.entity.TripBreadcrumb;
import com.ridehailing.rideservice.exception.InvalidRideStateException;
import com.ridehailing.rideservice.exception.RideNotFoundException;
import com.ridehailing.rideservice.repository.RideRepository;
import com.ridehailing.rideservice.repository.TripBreadcrumbRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TripTrailService {

    private final RideRepository rideRepository;
    private final TripBreadcrumbRepository breadcrumbRepository;

    public ApiResponse<Void> addBreadcrumb(UUID rideId, BreadcrumbRequest request) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found"));

        if (ride.getStatus() != RideStatus.ACCEPTED && ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new InvalidRideStateException(
                    "Breadcrumbs are only recorded while a ride is accepted or in progress");
        }

        breadcrumbRepository.save(TripBreadcrumb.builder()
                .ride(ride)
                .lat(request.getLat())
                .lng(request.getLng())
                .recordedAt(Instant.now())
                .build());

        return ApiResponse.success("Breadcrumb recorded", null);
    }

    public ApiResponse<TrailResponse> getTrail(UUID rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found"));

        List<BreadcrumbPoint> points = breadcrumbRepository
                .findByRideIdOrderByRecordedAtAsc(rideId)
                .stream()
                .map(b -> BreadcrumbPoint.builder()
                        .lat(b.getLat())
                        .lng(b.getLng())
                        .recordedAt(b.getRecordedAt())
                        .build())
                .toList();

        return ApiResponse.success("Trip trail", TrailResponse.builder()
                .rideId(rideId)
                .status(ride.getStatus())
                .pointCount(points.size())
                .points(points)
                .build());
    }
}
