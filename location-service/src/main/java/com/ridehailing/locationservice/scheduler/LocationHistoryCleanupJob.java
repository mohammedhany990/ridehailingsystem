package com.ridehailing.locationservice.scheduler;

import com.ridehailing.locationservice.repository.LocationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationHistoryCleanupJob {

    private final LocationHistoryRepository locationHistoryRepository;

    @Value("${app.jobs.location-history-retention-days:7}")
    private long retentionDays;

    @Scheduled(cron = "${app.jobs.location-history-cleanup-cron:0 0 3 * * *}")
    @SchedulerLock(name = "locationHistoryCleanup", lockAtLeastFor = "PT30S", lockAtMostFor = "PT10M")
    public void deleteOldHistory() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        long deleted = locationHistoryRepository.deleteByCreatedAtBefore(cutoff);
        log.info("Location history cleanup removed {} records older than {}", deleted, cutoff);
    }
}
