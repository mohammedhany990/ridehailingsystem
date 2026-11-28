package com.ridehailing.rideservice.controller;

import com.ridehailing.rideservice.dto.AdminStatsResponse;
import com.ridehailing.rideservice.entity.RideStatus;
import com.ridehailing.rideservice.repository.RideRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminStatsControllerTest {

    @Mock
    private RideRepository rideRepository;

    @InjectMocks
    private AdminStatsController controller;

    @Test
    void statsAggregatesCounts() {
        when(rideRepository.count()).thenReturn(20L);
        when(rideRepository.countByRequestedAtAfter(any(Instant.class))).thenReturn(5L);
        when(rideRepository.countByStatus(RideStatus.REQUESTED)).thenReturn(2L);
        when(rideRepository.countByStatus(RideStatus.ACCEPTED)).thenReturn(1L);
        when(rideRepository.countByStatus(RideStatus.IN_PROGRESS)).thenReturn(3L);
        when(rideRepository.countByStatus(RideStatus.COMPLETED)).thenReturn(12L);
        when(rideRepository.countByStatus(RideStatus.CANCELLED)).thenReturn(2L);

        var response = controller.stats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AdminStatsResponse stats = response.getBody().getData();
        assertEquals(20L, stats.totalRides());
        assertEquals(5L, stats.ridesToday());
        assertEquals(6L, stats.activeRides());
        assertEquals(12L, stats.ridesByStatus().get("COMPLETED"));
    }
}
