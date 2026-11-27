package com.ridehailing.rideservice.jobs;

import com.ridehailing.rideservice.entity.FareOffer;
import com.ridehailing.rideservice.entity.FareOfferStatus;
import com.ridehailing.rideservice.entity.Ride;
import com.ridehailing.rideservice.entity.RideStatus;
import com.ridehailing.rideservice.kafka.RideEventPublisher;
import com.ridehailing.rideservice.repository.FareOfferRepository;
import com.ridehailing.rideservice.repository.RideRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaleRideExpiryJobTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private FareOfferRepository fareOfferRepository;

    @Mock
    private RideEventPublisher rideEventPublisher;

    @InjectMocks
    private StaleRideExpiryJob job;

    private Ride staleRide() {
        return Ride.builder()
                .id(UUID.randomUUID())
                .riderId(UUID.randomUUID())
                .driverId(UUID.randomUUID())
                .pickupLat(30.0).pickupLng(31.0)
                .dropoffLat(30.1).dropoffLng(31.1)
                .status(RideStatus.REQUESTED)
                .requestedAt(Instant.now().minus(10, ChronoUnit.MINUTES))
                .build();
    }

    @Test
    void staleRideIsCancelledAndPendingOffersExpire() {
        ReflectionTestUtils.setField(job, "staleTimeoutMinutes", 5L);
        Ride ride = staleRide();
        FareOffer offer = FareOffer.builder()
                .id(UUID.randomUUID()).ride(ride).driverId(ride.getDriverId())
                .amount(new BigDecimal("60.00")).build();
        when(rideRepository.findByStatusAndRequestedAtBefore(eq(RideStatus.REQUESTED), any()))
                .thenReturn(List.of(ride));
        when(fareOfferRepository.findByRideIdAndStatus(ride.getId(), FareOfferStatus.PENDING))
                .thenReturn(List.of(offer));

        job.expireStaleRides();

        assertThat(ride.getStatus()).isEqualTo(RideStatus.CANCELLED);
        assertThat(offer.getStatus()).isEqualTo(FareOfferStatus.EXPIRED);
        verify(fareOfferRepository).save(offer);
        verify(rideRepository).save(ride);
        verify(rideEventPublisher).publish(argThat(e ->
                e.eventType().equals("RIDE_CANCELLED") && e.rideId().equals(ride.getId())));
    }

    @Test
    void noStaleRidesMeansNoWork() {
        ReflectionTestUtils.setField(job, "staleTimeoutMinutes", 5L);
        when(rideRepository.findByStatusAndRequestedAtBefore(eq(RideStatus.REQUESTED), any()))
                .thenReturn(List.of());

        job.expireStaleRides();

        verifyNoInteractions(fareOfferRepository);
        verifyNoInteractions(rideEventPublisher);
    }
}
