package com.ridehailing.rideservice.controller;

import com.ridehailing.rideservice.common.ApiResponse;
import com.ridehailing.rideservice.dto.FareOfferRequest;
import com.ridehailing.rideservice.dto.FareOfferResponse;
import com.ridehailing.rideservice.dto.ProposedFareUpdate;
import com.ridehailing.rideservice.service.FareNegotiationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rides/{rideId}/offers")
@RequiredArgsConstructor
public class FareOfferController {

    private final FareNegotiationService fareNegotiationService;

    @PostMapping
    public ResponseEntity<ApiResponse<FareOfferResponse>> makeOffer(
            @PathVariable UUID rideId,
            @Valid @RequestBody FareOfferRequest request
    ) {
        FareOfferResponse offer = fareNegotiationService.driverOffer(
                rideId, request.getDriverId(), request.getAmount());
        return ResponseEntity.ok(ApiResponse.success("Fare offer sent to rider", offer));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FareOfferResponse>>> listOffers(
            @PathVariable UUID rideId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID riderId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(ApiResponse.success(
                "Offers fetched", fareNegotiationService.listOffers(rideId, riderId)));
    }

    @PutMapping("/{offerId}/accept")
    public ResponseEntity<ApiResponse<FareOfferResponse>> acceptOffer(
            @PathVariable UUID rideId,
            @PathVariable UUID offerId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID riderId = UUID.fromString(jwt.getSubject());
        FareOfferResponse offer = fareNegotiationService.acceptOffer(rideId, offerId, riderId);
        return ResponseEntity.ok(ApiResponse.success("Offer accepted, driver assigned", offer));
    }

    @PutMapping("/{offerId}/decline")
    public ResponseEntity<ApiResponse<FareOfferResponse>> declineOffer(
            @PathVariable UUID rideId,
            @PathVariable UUID offerId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID riderId = UUID.fromString(jwt.getSubject());
        FareOfferResponse offer = fareNegotiationService.declineOffer(rideId, offerId, riderId);
        return ResponseEntity.ok(ApiResponse.success("Offer declined", offer));
    }
}
