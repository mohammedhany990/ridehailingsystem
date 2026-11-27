package com.ridehailing.rideservice.controller;

import com.ridehailing.rideservice.common.ApiResponse;
import com.ridehailing.rideservice.dto.BreadcrumbRequest;
import com.ridehailing.rideservice.dto.TrailResponse;
import com.ridehailing.rideservice.service.TripTrailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Tag(name = "Trip Tracking", description = "Trip breadcrumbs, trail and replay")
public class TripController {

    private final TripTrailService tripTrailService;

    @PostMapping("/{rideId}/breadcrumbs")
    @Operation(summary = "Record a GPS breadcrumb for an active ride")
    public ResponseEntity<ApiResponse<Void>> addBreadcrumb(
            @PathVariable UUID rideId,
            @Valid @RequestBody BreadcrumbRequest request) {
        return ResponseEntity.ok(tripTrailService.addBreadcrumb(rideId, request));
    }

    @GetMapping("/{rideId}/trail")
    @Operation(summary = "Get the recorded breadcrumb trail for a ride")
    public ResponseEntity<ApiResponse<TrailResponse>> getTrail(@PathVariable UUID rideId) {
        return ResponseEntity.ok(tripTrailService.getTrail(rideId));
    }
}
