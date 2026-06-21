package com.ridehailing.ratingservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RatingSummaryResponse {
    private double averageScore;
    private long totalRatings;
}