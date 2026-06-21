package com.ridehailing.ratingservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class RatingResponse {
    private UUID id;
    private UUID rideId;
    private UUID raterId;
    private UUID rateeId;
    private String raterType;
    private String rateeType;
    private Integer score;
    private String comment;
    private Instant createdAt;
}