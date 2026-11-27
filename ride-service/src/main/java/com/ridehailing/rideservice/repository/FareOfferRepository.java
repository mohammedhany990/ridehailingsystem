package com.ridehailing.rideservice.repository;

import com.ridehailing.rideservice.entity.FareOffer;
import com.ridehailing.rideservice.entity.FareOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FareOfferRepository extends JpaRepository<FareOffer, UUID> {

    List<FareOffer> findByRideIdOrderByCreatedAtDesc(UUID rideId);

    List<FareOffer> findByRideIdAndDriverIdAndStatus(UUID rideId, UUID driverId, FareOfferStatus status);

    List<FareOffer> findByRideIdAndStatus(UUID rideId, FareOfferStatus status);

    Optional<FareOffer> findByIdAndRideId(UUID id, UUID rideId);
}
