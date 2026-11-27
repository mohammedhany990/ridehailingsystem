package com.ridehailing.rideservice.service;

import com.ridehailing.rideservice.dto.FareOfferResponse;
import com.ridehailing.rideservice.entity.FareOffer;
import com.ridehailing.rideservice.entity.FareOfferStatus;
import com.ridehailing.rideservice.entity.Ride;
import com.ridehailing.rideservice.entity.RideStatus;
import com.ridehailing.rideservice.event.RideEvent;
import com.ridehailing.rideservice.exception.InvalidRideStateException;
import com.ridehailing.rideservice.exception.RideNotFoundException;
import com.ridehailing.rideservice.kafka.RideEventPublisher;
import com.ridehailing.rideservice.repository.FareOfferRepository;
import com.ridehailing.rideservice.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FareNegotiationServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private FareOfferRepository fareOfferRepository;

    @Mock
    private RideEventPublisher rideEventPublisher;

    @InjectMocks
    private FareNegotiationService service;

    private UUID riderId;
    private UUID driverId;
    private Ride ride;

    @BeforeEach
    void setUp() {
        riderId = UUID.randomUUID();
        driverId = UUID.randomUUID();
        ride = Ride.builder()
                .id(UUID.randomUUID())
                .riderId(riderId)
                .driverId(driverId)
                .pickupLat(30.0).pickupLng(31.0)
                .dropoffLat(30.1).dropoffLng(31.1)
                .status(RideStatus.REQUESTED)
                .proposedFare(new BigDecimal("50.00"))
                .build();
    }

    private void stubRide() {
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
    }

    @Test
    void driverOfferCreatesPendingOfferAndPublishesEvent() {
        stubRide();
        when(fareOfferRepository.findByRideIdAndDriverIdAndStatus(ride.getId(), driverId, FareOfferStatus.PENDING))
                .thenReturn(List.of());
        when(fareOfferRepository.save(any(FareOffer.class))).thenAnswer(i -> i.getArgument(0));

        FareOfferResponse response = service.driverOffer(ride.getId(), driverId, new BigDecimal("65.00"));

        assertThat(response.getAmount()).isEqualByComparingTo("65.00");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        verify(rideEventPublisher).publish(argThat(e ->
                e.eventType().equals("FARE_OFFERED") && "65.00".equals(e.reason())));
    }

    @Test
    void newDriverOfferSupersedesPreviousPendingOffer() {
        stubRide();
        FareOffer previous = FareOffer.builder()
                .id(UUID.randomUUID()).ride(ride).driverId(driverId)
                .amount(new BigDecimal("60.00")).build();
        when(fareOfferRepository.findByRideIdAndDriverIdAndStatus(ride.getId(), driverId, FareOfferStatus.PENDING))
                .thenReturn(List.of(previous));
        when(fareOfferRepository.save(any(FareOffer.class))).thenAnswer(i -> i.getArgument(0));

        service.driverOffer(ride.getId(), driverId, new BigDecimal("70.00"));

        assertThat(previous.getStatus()).isEqualTo(FareOfferStatus.SUPERSEDED);
    }

    @Test
    void offerFromAnotherDriverIsRejected() {
        stubRide();
        UUID stranger = UUID.randomUUID();

        assertThatThrownBy(() -> service.driverOffer(ride.getId(), stranger, new BigDecimal("60.00")))
                .isInstanceOf(InvalidRideStateException.class);
        verify(fareOfferRepository, never()).save(any());
    }

    @Test
    void acceptOfferAssignsDriverAndSetsNegotiatedAmount() {
        stubRide();
        FareOffer offer = FareOffer.builder()
                .id(UUID.randomUUID()).ride(ride).driverId(driverId)
                .amount(new BigDecimal("65.00")).build();
        when(fareOfferRepository.findByIdAndRideId(offer.getId(), ride.getId()))
                .thenReturn(Optional.of(offer));
        when(fareOfferRepository.findByRideIdAndStatus(ride.getId(), FareOfferStatus.PENDING))
                .thenReturn(List.of(offer));

        FareOfferResponse response = service.acceptOffer(ride.getId(), offer.getId(), riderId);

        assertThat(response.getStatus()).isEqualTo("ACCEPTED");
        assertThat(ride.getStatus()).isEqualTo(RideStatus.ACCEPTED);
        assertThat(ride.getNegotiatedAmount()).isEqualByComparingTo("65.00");
        assertThat(ride.getDriverId()).isEqualTo(driverId);
        verify(rideEventPublisher).publish(argThat(e -> e.eventType().equals("FARE_OFFER_ACCEPTED")));
        verify(rideEventPublisher).publish(argThat(e -> e.eventType().equals("RIDE_ACCEPTED")));
    }

    @Test
    void acceptingAnOfferSupersedesOtherPendingOffers() {
        stubRide();
        FareOffer winner = FareOffer.builder()
                .id(UUID.randomUUID()).ride(ride).driverId(driverId)
                .amount(new BigDecimal("65.00")).build();
        FareOffer loser = FareOffer.builder()
                .id(UUID.randomUUID()).ride(ride).driverId(driverId)
                .amount(new BigDecimal("70.00")).build();
        when(fareOfferRepository.findByIdAndRideId(winner.getId(), ride.getId()))
                .thenReturn(Optional.of(winner));
        when(fareOfferRepository.findByRideIdAndStatus(ride.getId(), FareOfferStatus.PENDING))
                .thenReturn(List.of(winner, loser));

        service.acceptOffer(ride.getId(), winner.getId(), riderId);

        assertThat(loser.getStatus()).isEqualTo(FareOfferStatus.SUPERSEDED);
    }

    @Test
    void declineOfferMarksItDeclinedAndNotifiesDriver() {
        stubRide();
        FareOffer offer = FareOffer.builder()
                .id(UUID.randomUUID()).ride(ride).driverId(driverId)
                .amount(new BigDecimal("80.00")).build();
        when(fareOfferRepository.findByIdAndRideId(offer.getId(), ride.getId()))
                .thenReturn(Optional.of(offer));
        when(fareOfferRepository.save(any(FareOffer.class))).thenAnswer(i -> i.getArgument(0));

        FareOfferResponse response = service.declineOffer(ride.getId(), offer.getId(), riderId);

        assertThat(response.getStatus()).isEqualTo("DECLINED");
        verify(rideEventPublisher).publish(argThat(e ->
                e.eventType().equals("FARE_OFFER_DECLINED") && driverId.equals(e.driverId())));
    }

    @Test
    void nonRiderCannotManageOffers() {
        stubRide();
        UUID stranger = UUID.randomUUID();

        assertThatThrownBy(() -> service.listOffers(ride.getId(), stranger))
                .isInstanceOf(InvalidRideStateException.class);
    }

    @Test
    void offersAreBlockedOnceRideIsAccepted() {
        ride.accept(driverId);
        stubRide();

        assertThatThrownBy(() -> service.driverOffer(ride.getId(), driverId, new BigDecimal("60.00")))
                .isInstanceOf(InvalidRideStateException.class);
    }

    @Test
    void riderCounterUpdatesProposedFareAndNotifiesDriver() {
        stubRide();

        service.updateProposedFare(ride.getId(), riderId, new BigDecimal("55.00"));

        assertThat(ride.getProposedFare()).isEqualByComparingTo("55.00");
        verify(rideEventPublisher).publish(argThat(e ->
                e.eventType().equals("FARE_COUNTERED") && "55.00".equals(e.reason())));
    }

    @Test
    void missingRideThrowsNotFound() {
        UUID missing = UUID.randomUUID();
        when(rideRepository.findById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listOffers(missing, riderId))
                .isInstanceOf(RideNotFoundException.class);
    }
}
