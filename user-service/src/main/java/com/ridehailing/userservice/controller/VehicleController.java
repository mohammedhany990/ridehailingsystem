package com.ridehailing.userservice.controller;

import com.ridehailing.userservice.common.ApiResponse;
import com.ridehailing.userservice.dto.CreateVehicleRequest;
import com.ridehailing.userservice.dto.UpdateVehicleRequest;
import com.ridehailing.userservice.dto.VehicleResponse;
import com.ridehailing.userservice.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateVehicleRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        return ResponseEntity
                .ok(vehicleService.createVehicle(userId, request));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<VehicleResponse>> getMyVehicle(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        return ResponseEntity
                .ok(vehicleService.getMyVehicle(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateVehicleRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        return ResponseEntity
                .ok(vehicleService.updateVehicle(userId, request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> deleteVehicle(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        return ResponseEntity
                .ok(vehicleService.deleteVehicle(userId));
    }
}