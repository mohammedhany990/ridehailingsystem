package com.ridehailing.pricingservice.controller;

import com.ridehailing.pricingservice.dto.*;
import com.ridehailing.pricingservice.service.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
@Tag(name = "Pricing", description = "Fare estimation and calculation")
public class PricingController {

    private final PricingService pricingService;

    @PostMapping("/estimate")
    @Operation(summary = "Pre-ride fare estimate")
    public ResponseEntity<ApiResponse<FareEstimateResponse>> estimate(
            @Valid @RequestBody FareEstimateRequest request) {
        FareEstimateResponse estimate = pricingService.estimate(request);
        return ResponseEntity.ok(ApiResponse.success("Fare estimated", estimate));
    }

    @PostMapping("/calculate")
    @Operation(summary = "Post-ride fare calculation (called by ride-service)")
    public ResponseEntity<ApiResponse<FareCalculationResponse>> calculate(
            @Valid @RequestBody FareCalculationRequest request) {
        FareCalculationResponse response = pricingService.calculate(request);
        return ResponseEntity.ok(ApiResponse.success("Fare calculated", response));
    }

    @PostMapping("/promo/validate")
    @Operation(summary = "Validate a promo code")
    public ResponseEntity<ApiResponse<PromoValidationResponse>> validatePromo(
            @Valid @RequestBody PromoValidationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Promo validation", pricingService.validatePromo(request.getCode())));
    }

    @GetMapping("/ride/{rideId}")
    @Operation(summary = "Get calculated fare by ride ID")
    public ResponseEntity<ApiResponse<FareCalculationResponse>> getByRideId(
            @PathVariable UUID rideId) {
        return ResponseEntity.ok(pricingService.getByRideId(rideId));
    }
}
