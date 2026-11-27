package com.ridehailing.pricingservice.repository;

import com.ridehailing.pricingservice.entity.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PromoCodeRepository extends JpaRepository<PromoCode, UUID> {

    Optional<PromoCode> findByCodeIgnoreCaseAndActiveTrue(String code);
}
