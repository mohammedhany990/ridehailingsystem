package com.ridehailing.userservice.controller;

import com.ridehailing.userservice.common.ApiResponse;
import com.ridehailing.userservice.dto.DriverRatingResponse;
import com.ridehailing.userservice.dto.RateDriverRequest;
import com.ridehailing.userservice.service.DriverRatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("api/rating")
@RequiredArgsConstructor
public class DriverRatingController {
    private final DriverRatingService driverRatingService;


    @PostMapping
    public ApiResponse<DriverRatingResponse> rateDriver(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RateDriverRequest request
    ) {

        UUID passengerId = UUID.fromString(jwt.getClaimAsString("userId"));

        return driverRatingService.rateDriver(passengerId, request);
    }


    @GetMapping("/{driverId}")
    public ApiResponse<List<DriverRatingResponse>> getDriverRatings(@PathVariable UUID driverId) {

        return driverRatingService.getDriverRatings(driverId);
    }


}
