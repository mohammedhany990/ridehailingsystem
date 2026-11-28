package com.ridehailing.authservice.dto;

public record AdminStatsResponse(
        long totalUsers,
        long riders,
        long drivers,
        long admins,
        long activeUsers
) {
}
