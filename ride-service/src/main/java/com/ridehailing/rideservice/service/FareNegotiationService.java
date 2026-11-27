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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FareNegotiationService {

    private final RideRepository rideRepository;
    private final FareOfferRepository fareOfferRepository;
    private final RideEventPublisher rideEventPublisher;

    @Transactional
    public FareOfferResponse driverOffer(UUID rideId, UUID driverId, BigDecimal amount) {
        Ride ride = findRide(rideId);
        assertNegotiable(ride);
        if (!driverId.equals(ride.getDriverId())) {
            throw new InvalidRideStateException("Only the matched driver can make an offer on this ride");
        }

        fareOfferRepository
                .findByRideIdAndDriverIdAndStatus(rideId, driverId, FareOfferStatus.PENDING)
                .forEach(FareOffer::supersede);

        FareOffer offer = FareOffer.builder()
                .ride(ride)
                .driverId(driverId)
                .amount(amount)
                .build();
        FareOffer saved = fareOfferRepository.save(offer);

        rideEventPublisher.publish(RideEvent.of(
                "FARE_OFFERED", ride.getId(), ride.getRiderId(), driverId, amount.toPlainString()));

        return FareOfferResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<FareOfferResponse> listOffers(UUID rideId, UUID riderId) {
        Ride ride = findRide(rideId);
        assertRider(ride, riderId);
        return fareOfferRepository.findByRideIdOrderByCreatedAtDesc(rideId)
                .stream()
                .map(FareOfferResponse::from)
                .toList();
    }

    @Transactional
    public FareOfferResponse acceptOffer(UUID rideId, UUID offerId, UUID riderId) {
        Ride ride = findRide(rideId);
        assertRider(ride, riderId);
        assertNegotiable(ride);

        FareOffer offer = fareOfferRepository.findByIdAndRideId(offerId, rideId)
                .orElseThrow(() -> new RideNotFoundException("Offer not found for this ride"));

        offer.accept();
        ride.setNegotiatedAmount(offer.getAmount());
        ride.accept(offer.getDriverId());

        fareOfferRepository.findByRideIdAndStatus(rideId, FareOfferStatus.PENDING)
                .stream()
                .filter(o -> !o.getId().equals(offerId))
                .forEach(FareOffer::supersede);

        fareOfferRepository.save(offer);
        rideRepository.save(ride);

        rideEventPublisher.publish(RideEvent.of(
                "FARE_OFFER_ACCEPTED", ride.getId(), ride.getRiderId(), offer.getDriverId(),
                offer.getAmount().toPlainString()));
        rideEventPublisher.publish(RideEvent.of(
                "RIDE_ACCEPTED", ride.getId(), ride.getRiderId(), offer.getDriverId(), null));

        return FareOfferResponse.from(offer);
    }

    @Transactional
    public FareOfferResponse declineOffer(UUID rideId, UUID offerId, UUID riderId) {
        Ride ride = findRide(rideId);
        assertRider(ride, riderId);

        FareOffer offer = fareOfferRepository.findByIdAndRideId(offerId, rideId)
                .orElseThrow(() -> new RideNotFoundException("Offer not found for this ride"));

        offer.decline();
        FareOffer saved = fareOfferRepository.save(offer);

        rideEventPublisher.publish(RideEvent.of(
                "FARE_OFFER_DECLINED", ride.getId(), ride.getRiderId(), offer.getDriverId(),
                offer.getAmount().toPlainString()));

        return FareOfferResponse.from(saved);
    }

    @Transactional
    public FareOfferResponse updateProposedFare(UUID rideId, UUID riderId, BigDecimal amount) {
        Ride ride = findRide(rideId);
        assertRider(ride, riderId);
        assertNegotiable(ride);

        ride.setProposedFare(amount);
        rideRepository.save(ride);

        rideEventPublisher.publish(RideEvent.of(
                "FARE_COUNTERED", ride.getId(), ride.getRiderId(), ride.getDriverId(),
                amount.toPlainString()));

        return fareOfferRepository
                .findByRideIdAndDriverIdAndStatus(rideId, ride.getDriverId(), FareOfferStatus.PENDING)
                .stream()
                .findFirst()
                .map(FareOfferResponse::from)
                .orElse(null);
    }

    private Ride findRide(UUID rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException("Ride not found: " + rideId));
    }

    private void assertRider(Ride ride, UUID riderId) {
        if (!riderId.equals(ride.getRiderId())) {
            throw new InvalidRideStateException("Only the rider can manage offers on this ride");
        }
    }

    private void assertNegotiable(Ride ride) {
        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new InvalidRideStateException(
                    "Fare can only be negotiated while the ride is REQUESTED, current: " + ride.getStatus());
        }
    }
}
