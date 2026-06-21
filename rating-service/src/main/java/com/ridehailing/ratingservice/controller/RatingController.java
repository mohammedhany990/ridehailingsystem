package com.ridehailing.ratingservice.controller;

import com.ridehailing.ratingservice.dto.*;
import com.ridehailing.ratingservice.entity.PartyType;
import com.ridehailing.ratingservice.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@Tag(name = "Ratings", description = "Ride rating system")
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<ApiResponse<RatingResponse>> submitRating(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SubmitRatingRequest request) {

        UUID raterId = UUID.fromString(jwt.getClaimAsString("userId"));

        PartyType raterType = PartyType.valueOf(jwt.getClaimAsString("role"));

        return ResponseEntity.ok(
                ratingService.submitRating(raterId, raterType, request)
        );
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user ratings (paginated)")
    public ResponseEntity<ApiResponse<Page<RatingResponse>>> getUserRatings(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<RatingResponse> result = ratingService.getRatingsForUser(userId, page, size);

        return ResponseEntity.ok(ApiResponse.success("Ratings fetched", result));
    }

    @GetMapping("/ride/{rideId}")
    @Operation(summary = "Get ratings for ride")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getRideRatings(
            @PathVariable UUID rideId) {

        return ResponseEntity.ok(
                ApiResponse.success("Ratings fetched",
                        ratingService.getRatingsForRide(rideId))
        );
    }

    @GetMapping("/summary/{userId}")
    @Operation(summary = "Get rating summary (driver or rider)")
    public ResponseEntity<ApiResponse<RatingSummaryResponse>> getSummary(
            @PathVariable UUID userId,
            @RequestParam PartyType type) {

        return ResponseEntity.ok(
                ApiResponse.success("Summary fetched",
                        ratingService.getSummary(userId, type))
        );
    }
}