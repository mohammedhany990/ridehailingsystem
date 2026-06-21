package com.ridehailing.pricingservice.repository;

import com.ridehailing.pricingservice.entity.FareCalculation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FareCalculationRepository extends JpaRepository<FareCalculation, UUID> {
    Optional<FareCalculation> findByRideId(UUID rideId);
    boolean existsByRideId(UUID rideId);
}
