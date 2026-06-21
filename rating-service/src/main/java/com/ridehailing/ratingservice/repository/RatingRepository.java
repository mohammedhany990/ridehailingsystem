package com.ridehailing.ratingservice.repository;

import com.ridehailing.ratingservice.entity.PartyType;
import com.ridehailing.ratingservice.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<Rating, UUID> {

    Page<Rating> findByRateeId(UUID rateeId, Pageable pageable);

    List<Rating> findByRideId(UUID rideId);

    boolean existsByRideIdAndRaterId(UUID rideId, UUID raterId);

    @Query("""
                SELECT COALESCE(AVG(r.score), 0)
                FROM Rating r
                WHERE r.rateeId = :rateeId
                AND r.rateeType = :rateeType
            """)
    Optional<Double> findAverageScoreByRateeId(UUID rateeId, PartyType rateeType);

    @Query("""
                SELECT COUNT(r)
                FROM Rating r
                WHERE r.rateeId = :rateeId
                AND r.rateeType = :rateeType
            """)
    long countByRateeId(UUID rateeId, PartyType rateeType);
}