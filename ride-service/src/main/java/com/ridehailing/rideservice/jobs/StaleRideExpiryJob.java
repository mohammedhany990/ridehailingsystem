package com.ridehailing.rideservice.jobs;

import com.ridehailing.rideservice.entity.FareOfferStatus;
import com.ridehailing.rideservice.entity.Ride;
import com.ridehailing.rideservice.entity.RideStatus;
import com.ridehailing.rideservice.event.RideEvent;
import com.ridehailing.rideservice.kafka.RideEventPublisher;
import com.ridehailing.rideservice.repository.FareOfferRepository;
import com.ridehailing.rideservice.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StaleRideExpiryJob {

    private final RideRepository rideRepository;
    private final FareOfferRepository fareOfferRepository;
    private final RideEventPublisher rideEventPublisher;

    @Value("${app.jobs.stale-ride-timeout-minutes:5}")
    private long staleTimeoutMinutes;

    @Scheduled(fixedDelayString = "${app.jobs.stale-ride-scan-ms:60000}", initialDelay = 30000)
    @SchedulerLock(name = "staleRideExpiry", lockAtLeastFor = "PT10S", lockAtMostFor = "PT4M")
    @Transactional
    public void expireStaleRides() {
        Instant cutoff = Instant.now().minus(staleTimeoutMinutes, ChronoUnit.MINUTES);
        List<Ride> stale = rideRepository.findByStatusAndRequestedAtBefore(RideStatus.REQUESTED, cutoff);
        for (Ride ride : stale) {
            fareOfferRepository.findByRideIdAndStatus(ride.getId(), FareOfferStatus.PENDING)
                    .forEach(offer -> {
                        offer.expire();
                        fareOfferRepository.save(offer);
                    });
            ride.cancel("SYSTEM", "No driver accepted in time");
            rideRepository.save(ride);
            rideEventPublisher.publish(RideEvent.of(
                    "RIDE_CANCELLED", ride.getId(), ride.getRiderId(), ride.getDriverId(),
                    "No driver accepted in time"));
            log.info("Expired stale ride {}", ride.getId());
        }
    }
}
