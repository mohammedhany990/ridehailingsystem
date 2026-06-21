package com.ridehailing.userservice.repository;

import com.ridehailing.userservice.entity.DriverProfile;
import com.ridehailing.userservice.entity.DriverStatus;
import org.springframework.data.jpa.repository.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverProfileRepository extends JpaRepository<DriverProfile, UUID> {

    Optional<DriverProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    boolean existsByLicenseNumber(String licenseNumber);

    List<DriverProfile> findAllByStatus(DriverStatus status);

    long countByStatus(DriverStatus status);


    List<DriverProfile> findByUserIdIn(List<UUID> userIds);


    @Query("""
        SELECT d FROM DriverProfile d
        LEFT JOIN FETCH d.vehicle
        WHERE d.userId = :userId
    """)
    Optional<DriverProfile> findByUserIdWithVehicle(UUID userId);

    @Query("""
        SELECT d FROM DriverProfile d
        LEFT JOIN FETCH d.vehicle
        WHERE d.id = :id
    """)
    Optional<DriverProfile> findByIdWithVehicle(UUID id);
}