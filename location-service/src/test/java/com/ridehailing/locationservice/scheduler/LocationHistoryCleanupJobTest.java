package com.ridehailing.locationservice.scheduler;

import com.ridehailing.locationservice.repository.LocationHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationHistoryCleanupJobTest {

    @Mock
    private LocationHistoryRepository locationHistoryRepository;

    @InjectMocks
    private LocationHistoryCleanupJob job;

    @Test
    void deletesHistoryOlderThanRetention() {
        ReflectionTestUtils.setField(job, "retentionDays", 7L);
        when(locationHistoryRepository.deleteByCreatedAtBefore(any())).thenReturn(42L);

        job.deleteOldHistory();

        Instant expectedFloor = Instant.now().minus(8, ChronoUnit.DAYS);
        verify(locationHistoryRepository).deleteByCreatedAtBefore(argThat(cutoff ->
                cutoff.isAfter(expectedFloor)
                        && cutoff.isBefore(Instant.now().minus(6, ChronoUnit.DAYS))));
    }
}
