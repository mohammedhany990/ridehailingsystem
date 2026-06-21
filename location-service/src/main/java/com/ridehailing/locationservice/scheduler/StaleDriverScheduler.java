package com.ridehailing.locationservice.scheduler;

import com.ridehailing.locationservice.client.UserServiceClient;
import com.ridehailing.locationservice.entity.DriverLocation;
import com.ridehailing.locationservice.repository.DriverLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class StaleDriverScheduler {
    private final DriverLocationRepository locationRepository;
    private final UserServiceClient userServiceClient;

    @Scheduled(fixedDelay = 60_000)
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
    }
}
