package com.ridehailing.pricingservice.repository;

import com.ridehailing.pricingservice.entity.FareRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FareRuleRepository extends JpaRepository<FareRule, UUID> {
    Optional<FareRule> findByVehicleTypeAndActiveTrue(String vehicleType);
}
