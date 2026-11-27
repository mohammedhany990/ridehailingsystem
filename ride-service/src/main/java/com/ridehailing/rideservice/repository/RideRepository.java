package com.ridehailing.rideservice.repository;

import com.ridehailing.rideservice.entity.Ride;
import com.ridehailing.rideservice.entity.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import java.time.Instant;

public interface RideRepository extends JpaRepository<Ride, UUID> {

    List<Ride> findByStatusAndRequestedAtBefore(RideStatus status, Instant cutoff);

    List<Ride> findByRiderIdOrderByRequestedAtDesc(UUID riderId);

    List<Ride> findByDriverIdOrderByRequestedAtDesc(UUID driverId);

    List<Ride> findByStatus(RideStatus status);

    @Query("SELECT r FROM Ride r LEFT JOIN FETCH r.fare LEFT JOIN FETCH r.cancellation WHERE r.id = :id")
    Optional<Ride> findByIdWithDetails(@Param("id") UUID id);

    boolean existsByDriverIdAndStatusIn(UUID driverId, List<RideStatus> statuses);
}
