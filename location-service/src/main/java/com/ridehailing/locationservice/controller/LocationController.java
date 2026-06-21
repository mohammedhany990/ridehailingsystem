package com.ridehailing.locationservice.controller;

import com.ridehailing.locationservice.common.ApiResponse;
import com.ridehailing.locationservice.dto.*;
import com.ridehailing.locationservice.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
@Tag(name = "Location", description = "Driver location management")
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "Update driver location")
    @PostMapping("/update")
    public ResponseEntity<ApiResponse<LocationResponse>> update(
            @Valid @RequestBody UpdateLocationRequest request) {
        return ResponseEntity.ok(locationService.updateLocation(request));
    }

    @Operation(summary = "Get current driver location")
    @GetMapping("/{driverId}")
    public ResponseEntity<ApiResponse<LocationResponse>> get(
            @PathVariable String driverId) {
        return ResponseEntity.ok(locationService.getLocation(driverId));
    }

    @Operation(summary = "Find nearby available drivers")
    @PostMapping("/nearby")
    public ResponseEntity<ApiResponse<List<NearbyDriversResponse>>> nearby(
            @Valid @RequestBody NearbyDriversRequest request) {
        return ResponseEntity.ok(locationService.findNearbyDrivers(request));
    }


    @Operation(summary = "Get location history for a driver (last 50 points)")
    @GetMapping("/{driverId}/history")
    public ResponseEntity<ApiResponse<List<LocationHistoryResponse>>> history(
            @PathVariable String driverId) {
        return ResponseEntity.ok(locationService.getHistory(driverId));
    }

    @Operation(summary = "Driver heartbeat (keep-alive)")
    @PostMapping("/heartbeat/{driverId}")
    public ResponseEntity<ApiResponse<Void>> heartbeat(@PathVariable String driverId) {
        return ResponseEntity.ok(locationService.heartbeat(driverId));
    }

    @Operation(summary = "Remove driver from location service")
    @DeleteMapping("/{driverId}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable String driverId) {
        return ResponseEntity.ok(locationService.removeDriverLocation(driverId));
    }
}
