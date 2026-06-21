package com.ridehailing.ratingservice.service;

import com.ridehailing.ratingservice.dto.*;
import com.ridehailing.ratingservice.entity.PartyType;
import com.ridehailing.ratingservice.entity.Rating;
import com.ridehailing.ratingservice.repository.RatingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RatingService {

    private final RatingRepository ratingRepository;

    public ApiResponse<RatingResponse> submitRating(UUID raterId, PartyType raterType,
                                                    SubmitRatingRequest request) {

        if (ratingRepository.existsByRideIdAndRaterId(request.getRideId(), raterId)) {
            return ApiResponse.error("You already rated this ride");
        }

        Rating rating = Rating.builder()
                .rideId(request.getRideId())
                .raterId(raterId)
                .rateeId(request.getRateeId())
                .raterType(raterType)
                .rateeType(request.getRateeType())
                .score(request.getRating())
                .comment(request.getComment())
                .build();

        Rating saved = ratingRepository.save(rating);

        log.info("Rating created: rideId={}, raterId={}, rateeId={}, score={}",
                saved.getRideId(),
                saved.getRaterId(),
                saved.getRateeId(),
                saved.getScore());

        return ApiResponse.success("Rating submitted successfully", toResponse(saved));
    }

    public Page<RatingResponse> getRatingsForUser(UUID rateeId, int page, int size) {
        return ratingRepository.findByRateeId(
                rateeId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).map(this::toResponse);
    }

    public List<RatingResponse> getRatingsForRide(UUID rideId) {
        return ratingRepository.findByRideId(rideId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public RatingSummaryResponse getSummary(UUID rateeId, PartyType rateeType) {

        double avg = ratingRepository
                .findAverageScoreByRateeId(rateeId, rateeType)
                .orElse(0.0);

        long count = ratingRepository.countByRateeId(rateeId, rateeType);

        return RatingSummaryResponse.builder()
                .averageScore(Math.round(avg * 10.0) / 10.0)
                .totalRatings(count)
                .build();
    }

    private RatingResponse toResponse(Rating r) {
        return RatingResponse.builder()
                .id(r.getId())
                .rideId(r.getRideId())
                .raterId(r.getRaterId())
                .rateeId(r.getRateeId())
                .raterType(r.getRaterType().name())
                .rateeType(r.getRateeType().name())
                .score(r.getScore())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}