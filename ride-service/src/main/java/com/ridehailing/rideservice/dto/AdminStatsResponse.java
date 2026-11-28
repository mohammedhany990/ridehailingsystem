package com.ridehailing.rideservice.dto;

import java.util.Map;

public record AdminStatsResponse(
        long totalRides,
        long ridesToday,
        long activeRides,
        Map<String, Long> ridesByStatus
) {
}
