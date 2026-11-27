package com.ridehailing.locationservice.scheduler;

import com.ridehailing.locationservice.client.UserServiceClient;
import com.ridehailing.locationservice.entity.DriverLocation;
import com.ridehailing.locationservice.repository.DriverLocationRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StaleDriverScheduler {

    private final DriverLocationRepository locationRepository;
    private final UserServiceClient userServiceClient;

    @Scheduled(fixedDelay = 60_000)
    @SchedulerLock(name = "staleDriverOffline", lockAtLeastFor = "PT10S", lockAtMostFor = "PT3M")
    @CircuitBreaker(name = "user", fallbackMethod = "fallback")
    public void markStaleDriversOffline() {

        Instant cutoff = Instant.now().minusSeconds(120);

        List<DriverLocation> staleDrivers =
                locationRepository.findByLastHeartbeatBefore(cutoff);

        if (staleDrivers.isEmpty()) {
            return;
        }

        List<UUID> driverIds = staleDrivers.stream()
                .map(location -> UUID.fromString(location.getDriverId()))
                .toList();

        userServiceClient.markDriversOffline(driverIds);

        log.info("Marked {} stale drivers offline.", driverIds.size());
    }

    public void fallback(Throwable t) {
        log.error("Failed to mark stale drivers offline: {}", t.getMessage(), t);
    }
}