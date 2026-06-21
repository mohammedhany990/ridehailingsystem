package com.ridehailing.userservice.repository;


import com.ridehailing.userservice.entity.DriverRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DriverRatingRepository extends JpaRepository<DriverRating, UUID> {

    boolean existsByRideId(UUID rideId);

    List<DriverRating> findAllByDriverId(UUID driverId);
}