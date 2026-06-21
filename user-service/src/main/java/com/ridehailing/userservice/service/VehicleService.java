package com.ridehailing.userservice.service;


import com.ridehailing.userservice.common.ApiResponse;
import com.ridehailing.userservice.dto.CreateVehicleRequest;
import com.ridehailing.userservice.dto.UpdateVehicleRequest;
import com.ridehailing.userservice.dto.VehicleResponse;
import com.ridehailing.userservice.entity.DriverProfile;
import com.ridehailing.userservice.entity.Vehicle;
import com.ridehailing.userservice.exception.DriverProfileNotFoundException;
import com.ridehailing.userservice.exception.PlateNumberAlreadyExistsException;
import com.ridehailing.userservice.repository.DriverProfileRepository;
import com.ridehailing.userservice.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverProfileRepository driverProfileRepository;


    public ApiResponse<VehicleResponse> createVehicle(
            UUID userId,
            CreateVehicleRequest request
    ) {

        DriverProfile driver = driverProfileRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new DriverProfileNotFoundException("Driver profile not found"));

        if (driver.getVehicle() != null) {
            throw new RuntimeException("Driver already has a vehicle");
        }

        if (vehicleRepository.existsByPlateNumber(request.getPlateNumber())) {
            throw new PlateNumberAlreadyExistsException("Plate number already exists");
        }

        Vehicle vehicle = Vehicle.builder()
                .driverProfile(driver)
                .plateNumber(request.getPlateNumber())
                .make(request.getMake())
                .model(request.getModel())
                .color(request.getColor())
                .year(request.getYear())
                .createdAt(LocalDateTime.now())
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);

        driver.setVehicle(saved);

        return ApiResponse.<VehicleResponse>builder()
                .success(true)
                .message("Vehicle created successfully")
                .data(mapToResponse(saved))
                .build();
    }



    public ApiResponse<VehicleResponse> getMyVehicle(UUID userId) {

        Vehicle vehicle = vehicleRepository.findByDriverProfile_UserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Vehicle not found"));

        return ApiResponse.<VehicleResponse>builder()
                .success(true)
                .message("Vehicle fetched successfully")
                .data(mapToResponse(vehicle))
                .build();
    }



    public ApiResponse<VehicleResponse> updateVehicle(
            UUID userId,
            UpdateVehicleRequest request
    ) {

        Vehicle vehicle = vehicleRepository.findByDriverProfile_UserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Vehicle not found"));

        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setColor(request.getColor());
        vehicle.setYear(request.getYear());

        return ApiResponse.<VehicleResponse>builder()
                .success(true)
                .message("Vehicle updated successfully")
                .data(mapToResponse(vehicle))
                .build();
    }



    public ApiResponse<String> deleteVehicle(UUID userId) {

        Vehicle vehicle = vehicleRepository.findByDriverProfile_UserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Vehicle not found"));

        DriverProfile driver = vehicle.getDriverProfile();

        driver.setVehicle(null);

        vehicleRepository.delete(vehicle);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Vehicle deleted successfully")
                .data(vehicle.getId().toString())
                .build();
    }



    private VehicleResponse mapToResponse(Vehicle vehicle) {

        return VehicleResponse.builder()
                .id(vehicle.getId())
                .plateNumber(vehicle.getPlateNumber())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .color(vehicle.getColor())
                .year(vehicle.getYear())
                .createdAt(vehicle.getCreatedAt())
                .build();
    }
}