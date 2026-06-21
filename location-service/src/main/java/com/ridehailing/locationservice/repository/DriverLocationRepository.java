package com.ridehailing.locationservice.repository;

import com.ridehailing.locationservice.entity.DriverLocation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DriverLocationRepository extends MongoRepository<DriverLocation, String> {

    Optional<DriverLocation> findByDriverId(String driverId);

    List<DriverLocation> findByLastHeartbeatBefore(Instant cutoff);
}
