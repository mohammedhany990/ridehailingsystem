package com.ridehailing.rideservice.repository;

import com.ridehailing.rideservice.entity.TripBreadcrumb;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TripBreadcrumbRepository extends JpaRepository<TripBreadcrumb, UUID> {

    List<TripBreadcrumb> findByRideIdOrderByRecordedAtAsc(UUID rideId);
}
