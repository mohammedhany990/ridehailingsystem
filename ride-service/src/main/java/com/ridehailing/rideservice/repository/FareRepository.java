package com.ridehailing.rideservice.repository;

import com.ridehailing.rideservice.entity.Fare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FareRepository extends JpaRepository<Fare, UUID> {
    Optional<Fare> findByRideId(UUID rideId);
}

