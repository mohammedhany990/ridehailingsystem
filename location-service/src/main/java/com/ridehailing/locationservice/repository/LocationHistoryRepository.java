package com.ridehailing.locationservice.repository;

import com.ridehailing.locationservice.entity.LocationHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface LocationHistoryRepository extends MongoRepository<LocationHistory, String> {

    List<LocationHistory> findTop50ByDriverIdOrderByCreatedAtDesc(String driverId);

    long deleteByCreatedAtBefore(Instant cutoff);
}
