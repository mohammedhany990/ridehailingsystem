package com.ridehailing.paymentservice.repository;

import com.ridehailing.paymentservice.entity.Payment;
import java.util.List;
import java.time.Instant;
import com.ridehailing.paymentservice.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByRideId(UUID rideId);

    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    Page<Payment> findByRiderIdOrderByCreatedAtDesc(UUID riderId, Pageable pageable);

    Page<Payment> findByDriverIdOrderByCreatedAtDesc(UUID driverId, Pageable pageable);

    List<Payment> findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            PaymentStatus status, Instant from, Instant to);
}
