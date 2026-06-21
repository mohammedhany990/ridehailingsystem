package com.ridehailing.userservice.service;


import com.ridehailing.userservice.common.ApiResponse;
import com.ridehailing.userservice.dto.DriverRatingResponse;
import com.ridehailing.userservice.dto.RateDriverRequest;
import com.ridehailing.userservice.entity.DriverProfile;
import com.ridehailing.userservice.entity.DriverRating;
import com.ridehailing.userservice.repository.DriverProfileRepository;
import com.ridehailing.userservice.repository.DriverRatingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class DriverRatingService {

    private final DriverRatingRepository driverRatingRepository;
    private final DriverProfileRepository driverProfileRepository;


    public ApiResponse<DriverRatingResponse> rateDriver(UUID passengerId, RateDriverRequest request) {

        DriverProfile driver = driverProfileRepository.findById(request.getDriverId())
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (driverRatingRepository.existsByRideId(request.getRideId())) {
            throw new RuntimeException("Ride already rated");
        }

        DriverRating driverRating = DriverRating.builder()
                .driverId(driver.getId())
                .passengerId(passengerId)
                .rideId(request.getRideId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        DriverRating saved = driverRatingRepository.save(driverRating);

        updateDriverAverageRating(driver, request.getRating());

        return ApiResponse.<DriverRatingResponse>builder()
                .success(true)
                .message("Driver rated successfully")
                .data(mapToResponse(saved))
                .build();
    }

    public ApiResponse<List<DriverRatingResponse>> getDriverRatings(UUID driverId) {

        List<DriverRatingResponse> ratings =
                driverRatingRepository.findAllByDriverId(driverId)
                        .stream()
                        .map(this::mapToResponse)
                        .toList();

        return ApiResponse.<List<DriverRatingResponse>>builder()
                .success(true)
                .message("Driver ratings fetched successfully")
                .data(ratings)
                .build();
    }


    private void updateDriverAverageRating(DriverProfile driver, Integer newRating) {

        double currentAverage = driver.getRating();
        int totalRatings = driver.getTotalRatings();

        double updatedAverage =
                ((currentAverage * totalRatings) + newRating)
                        / (totalRatings + 1);

        driver.setRating(updatedAverage);
        driver.setTotalRatings(totalRatings + 1);
    }


    private DriverRatingResponse mapToResponse(DriverRating rating) {

        return DriverRatingResponse.builder()
                .id(rating.getId())
                .driverId(rating.getDriverId())
                .passengerId(rating.getPassengerId())
                .rideId(rating.getRideId())
                .rating(rating.getRating())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}