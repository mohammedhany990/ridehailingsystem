package com.ridehailing.rideservice.controller;

import com.ridehailing.rideservice.common.ApiResponse;
import com.ridehailing.rideservice.dto.AdminStatsResponse;
import com.ridehailing.rideservice.entity.RideStatus;
import com.ridehailing.rideservice.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;

@RestController
@RequestMapping("/api/rides/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final RideRepository rideRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminStatsResponse>> stats() {
        Instant startOfToday = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        Map<String, Long> byStatus = new java.util.LinkedHashMap<>();
        for (RideStatus status : RideStatus.values()) {
            byStatus.put(status.name(), rideRepository.countByStatus(status));
        }
        long active = byStatus.getOrDefault(RideStatus.REQUESTED.name(), 0L)
                + byStatus.getOrDefault(RideStatus.ACCEPTED.name(), 0L)
                + byStatus.getOrDefault(RideStatus.IN_PROGRESS.name(), 0L);
        AdminStatsResponse stats = new AdminStatsResponse(
                rideRepository.count(),
                rideRepository.countByRequestedAtAfter(startOfToday),
                active,
                byStatus
        );
        return ResponseEntity.ok(ApiResponse.success("Admin stats fetched", stats));
    }
}
