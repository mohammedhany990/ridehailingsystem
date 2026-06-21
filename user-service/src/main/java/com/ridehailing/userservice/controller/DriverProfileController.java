package com.ridehailing.userservice.controller;


import com.ridehailing.userservice.common.ApiResponse;
import com.ridehailing.userservice.dto.*;
import com.ridehailing.userservice.entity.DriverStatus;
import com.ridehailing.userservice.service.DriverProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/drivers")
public class DriverProfileController {

    private final DriverProfileService driverProfileService;


    @PostMapping
    public ResponseEntity<ApiResponse<DriverProfileResponse>> registerDriverProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateDriverRequest request
    ) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(driverProfileService.registerDriverProfile(userId, request));
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<DriverProfileResponse>>> getDriverProfiles() {

        return ResponseEntity
                .ok(driverProfileService.getDriverProfiles());
    }


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<DriverProfileDetailsResponse>> getMyProfile(@AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

        return ResponseEntity
                .ok(driverProfileService.getDriverProfile(userId));
    }


    @GetMapping("/me/status")
    public ResponseEntity<ApiResponse<String>> getDriverStatus(@AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

        return ResponseEntity
                .ok(driverProfileService.getDriverStatus(userId));
    }

    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<List<StatusResponse>>> getStatuses() {
        return ResponseEntity.ok(driverProfileService.getStatuses());
    }


    @PutMapping("/me/status")
    public ResponseEntity<ApiResponse<String>> updateDriverStatus(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateDriverStatusRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

        return ResponseEntity.ok(
                driverProfileService.updateDriverStatus(userId, request.getStatus())
        );
    }

    @PostMapping("/mark-offline")
    public void markDriversOffline(@RequestBody List<UUID> driverIds) {
        driverProfileService.markDriversOffline(driverIds);
    }


    @PutMapping("/me/online")
    public ResponseEntity<ApiResponse<String>> goOnline(@AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

        return ResponseEntity
                .ok(driverProfileService.goOnline(userId));
    }


    @PutMapping("/me/offline")
    public ResponseEntity<ApiResponse<String>> goOffline(@AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

        return ResponseEntity
                .ok(driverProfileService.goOffline(userId));
    }


    @PutMapping("/me/start-ride")
    public ResponseEntity<ApiResponse<String>> startRide(@AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

        return ResponseEntity
                .ok(driverProfileService.startRide(userId));
    }


    @PutMapping("/me/finish-ride")
    public ResponseEntity<ApiResponse<String>> finishRide(@AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

        return ResponseEntity
                .ok(driverProfileService.finishRide(userId));
    }


    @PutMapping("/me/location")
    public ResponseEntity<ApiResponse<String>> updateLocation(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Double lat,
            @RequestParam Double lng
    ) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

        return ResponseEntity
                .ok(driverProfileService.updateLocation(userId, lat, lng));
    }


    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> deleteDriverProfile(@AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

        return ResponseEntity
                .ok(driverProfileService.deleteDriverProfile(userId));
    }
}