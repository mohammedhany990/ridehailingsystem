package com.ridehailing.userservice.repository;

import com.ridehailing.userservice.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    boolean existsByPlateNumber(String plateNumber);

    Optional<Vehicle> findByDriverProfile_UserId(UUID userId);
}