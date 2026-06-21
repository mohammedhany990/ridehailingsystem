package com.ridehailing.rideservice.controller;


import com.ridehailing.rideservice.common.ApiResponse;
import com.ridehailing.rideservice.dto.*;
import com.ridehailing.rideservice.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;


    @PostMapping("/request")
    public ResponseEntity<ApiResponse<RideResponse>> requestRide(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RideRequest request,
            @RequestHeader("Authorization") String bearerToken
    ) {
        UUID riderId = UUID.fromString(jwt.getClaim("userId").toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rideService.requestRide(riderId, request, bearerToken));
    }


    @PutMapping("/{rideId}/accept")
    public ResponseEntity<ApiResponse<RideResponse>> acceptRide(
            @PathVariable UUID rideId,
            @Valid @RequestBody AcceptRideRequest request
    ) {
        return ResponseEntity.ok(rideService.acceptRide(rideId, request));
    }


    @PutMapping("/{rideId}/start")
    public ResponseEntity<ApiResponse<RideResponse>> startRide(
            @PathVariable UUID rideId,
            @RequestHeader("Authorization") String bearerToken
    ) {
        return ResponseEntity.ok(rideService.startRide(rideId, bearerToken));
    }


    @PutMapping("/{rideId}/complete")
    public ResponseEntity<ApiResponse<RideResponse>> completeRide(
            @PathVariable UUID rideId,
            @RequestHeader("Authorization") String bearerToken
    ) {
        return ResponseEntity.ok(rideService.completeRide(rideId, bearerToken));
    }


    @PutMapping("/{rideId}/cancel")
    public ResponseEntity<ApiResponse<RideResponse>> cancelRide(
            @PathVariable UUID rideId,
            @Valid @RequestBody CancelRideRequest request
    ) {
        return ResponseEntity.ok(rideService.cancelRide(rideId, request));
    }


    @GetMapping("/{rideId}")
    public ResponseEntity<ApiResponse<RideResponse>> getRide(@PathVariable UUID rideId) {
        return ResponseEntity
                .ok(rideService.getRide(rideId));
    }


    @GetMapping("/rider/history")
    public ResponseEntity<ApiResponse<List<RideResponse>>> getRiderHistory(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID riderId = UUID.fromString(jwt.getClaim("userId").toString());
        return ResponseEntity.ok(rideService.getRiderHistory(riderId));
    }


    @GetMapping("/driver/{driverId}/history")
    public ResponseEntity<ApiResponse<List<RideResponse>>> getDriverHistory(
            @PathVariable UUID driverId
    ) {
        return ResponseEntity.ok(rideService.getDriverHistory(driverId));
    }
}
