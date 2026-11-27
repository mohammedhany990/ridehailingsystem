package com.ridehailing.rideservice.controller;

import com.ridehailing.rideservice.common.ApiResponse;
import com.ridehailing.rideservice.dto.FareOfferResponse;
import com.ridehailing.rideservice.dto.ProposedFareUpdate;
import com.ridehailing.rideservice.service.FareNegotiationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rides/{rideId}/proposed-fare")
@RequiredArgsConstructor
public class ProposedFareController {

    private final FareNegotiationService fareNegotiationService;

    @PutMapping
    public ResponseEntity<ApiResponse<FareOfferResponse>> updateProposedFare(
            @PathVariable UUID rideId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ProposedFareUpdate request
    ) {
        UUID riderId = UUID.fromString(jwt.getSubject());
        FareOfferResponse pending = fareNegotiationService.updateProposedFare(
                rideId, riderId, request.getAmount());
        return ResponseEntity.ok(ApiResponse.success("Proposed fare updated", pending));
    }
}
