package com.ridehailing.userservice.service;

import com.ridehailing.userservice.common.ApiResponse;
import com.ridehailing.userservice.dto.*;
import com.ridehailing.userservice.entity.DriverProfile;
import com.ridehailing.userservice.entity.DriverStatus;
import com.ridehailing.userservice.entity.Vehicle;
import com.ridehailing.userservice.exception.DriverProfileAlreadyExistsException;
import com.ridehailing.userservice.exception.DriverProfileNotFoundException;
import com.ridehailing.userservice.exception.InvalidDriverStateException;
import com.ridehailing.userservice.exception.LicenseNumberAlreadyExistsException;
import com.ridehailing.userservice.repository.DriverProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class DriverProfileService {

    private final DriverProfileRepository driverProfileRepository;


    @Transactional
    public ApiResponse<DriverProfileResponse> registerDriverProfile(UUID userId, CreateDriverRequest request) {

        if (driverProfileRepository.existsByUserId(userId)) {
            throw new DriverProfileAlreadyExistsException("Driver profile already exists");
        }

        if (driverProfileRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new LicenseNumberAlreadyExistsException("License number already exists");
        }

        DriverProfile driver = DriverProfile.builder()
                .userId(userId)
                .licenseNumber(request.getLicenseNumber())
                .status(DriverStatus.OFFLINE)
                .createdAt(LocalDateTime.now())
                .build();

        DriverProfile saved = driverProfileRepository.save(driver);

        return ApiResponse.<DriverProfileResponse>builder()
                .success(true)
                .message("Driver profile created successfully")
                .data(mapToResponse(saved))
                .build();
    }


    @Transactional
    public ApiResponse<List<DriverProfileResponse>> getDriverProfiles() {

        List<DriverProfileResponse> result = driverProfileRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.<List<DriverProfileResponse>>builder()
                .success(true)
                .message("Drivers fetched successfully")
                .data(result)
                .build();
    }

    @Transactional
    public ApiResponse<DriverProfileDetailsResponse> getDriverProfile(UUID userId) {

        DriverProfile driver = driverProfileRepository.findByUserIdWithVehicle(userId)
                .orElseThrow(() -> new DriverProfileNotFoundException("Driver profile not found"));

        VehicleResponse vehicle = mapVehicle(driver.getVehicle());

        DriverProfileDetailsResponse response = DriverProfileDetailsResponse.builder()
                .id(driver.getId())
                .userId(driver.getUserId())
                .licenseNumber(driver.getLicenseNumber())
                .totalRides(driver.getTotalRides())
                .rating(driver.getRating())
                .totalRatings(driver.getTotalRatings())
                .createdAt(driver.getCreatedAt())
                .driverStatus(driver.getStatus())
                .currentLat(driver.getCurrentLat())
                .currentLng(driver.getCurrentLng())
                .locationUpdatedAt(driver.getLocationUpdatedAt())
                .vehicle(vehicle)
                .build();

        return ApiResponse.<DriverProfileDetailsResponse>builder()
                .success(true)
                .message("Driver profile found")
                .data(response)
                .build();
    }


    @Transactional(Transactional.TxType.SUPPORTS)
    public ApiResponse<String> getDriverStatus(UUID userId) {

        DriverProfile driver = findDriverByUserId(userId);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Driver status fetched successfully")
                .data(driver.getStatus().name())
                .build();
    }

    @Transactional
    public void markDriversOffline(List<UUID> driverIds) {

        List<DriverProfile> drivers =
                driverProfileRepository.findByUserIdIn(driverIds);

        drivers.forEach(driver ->
                driver.setStatus(DriverStatus.OFFLINE));

        driverProfileRepository.saveAll(drivers);
    }

    @Transactional
    public ApiResponse<String> updateDriverStatus(UUID userId, DriverStatus status) {

        DriverProfile driver = findDriverByUserId(userId);
        driver.setStatus(status);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Driver status updated")
                .data(driver.getStatus().name())
                .build();
    }

    public ApiResponse<List<StatusResponse>> getStatuses() {

        List<StatusResponse> statuses = Arrays.stream(DriverStatus.values())
                .map(s -> StatusResponse.builder()
                        .key(s.name())
                        .label(formatStatus(s))
                        .build()
                )
                .toList();

        return ApiResponse.<List<StatusResponse>>builder()
                .success(true)
                .data(statuses)
                .message("Available statuses")
                .build();
    }

    @Transactional
    public ApiResponse<String> goOnline(UUID userId) {

        DriverProfile driver = findDriverByUserId(userId);

        if (driver.getStatus() == DriverStatus.ON_RIDE) {
            throw new InvalidDriverStateException("Driver is currently on ride");
        }

        driver.goOnline();

        return ApiResponse.<String>builder()
                .success(true)
                .message("Driver is now online")
                .data(driver.getStatus().name())
                .build();
    }

    @Transactional
    public ApiResponse<String> goOffline(UUID userId) {

        DriverProfile driver = findDriverByUserId(userId);

        if (driver.getStatus() == DriverStatus.ON_RIDE) {
            throw new InvalidDriverStateException("Cannot go offline during ride");
        }

        driver.goOffline();

        return ApiResponse.<String>builder()
                .success(true)
                .message("Driver is now offline")
                .data(driver.getStatus().name())
                .build();
    }

    @Transactional
    public ApiResponse<String> startRide(UUID userId) {

        DriverProfile driver = findDriverByUserId(userId);

        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new InvalidDriverStateException("Driver not available");
        }

        driver.startRide();

        return ApiResponse.<String>builder()
                .success(true)
                .message("Ride started")
                .data(driver.getStatus().name())
                .build();
    }


    @Transactional
    public ApiResponse<String> finishRide(UUID userId) {

        DriverProfile driver = findDriverByUserId(userId);

        if (driver.getStatus() != DriverStatus.ON_RIDE) {
            throw new InvalidDriverStateException("Driver not on ride");
        }

        driver.finishRide();

        return ApiResponse.<String>builder()
                .success(true)
                .message("Ride finished")
                .data(driver.getStatus().name())
                .build();
    }


    @Transactional
    public ApiResponse<String> updateLocation(UUID userId, Double lat, Double lng) {

        validateLocation(lat, lng);

        DriverProfile driver = findDriverByUserId(userId);

        driver.updateLocation(lat, lng);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Location updated")
                .data("Lat: " + lat + ", Lng: " + lng)
                .build();
    }


    @Transactional
    public ApiResponse<String> deleteDriverProfile(UUID userId) {

        DriverProfile driver = findDriverByUserId(userId);
        driverProfileRepository.delete(driver);

        return ApiResponse.<String>builder()
                .success(true)
                .message("Driver deleted")
                .data(driver.getId().toString())
                .build();
    }

    private void validateLocation(Double lat, Double lng) {

        if (lat == null || lng == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }

        if (lat < -90 || lat > 90) {
            throw new IllegalArgumentException("Invalid latitude");
        }

        if (lng < -180 || lng > 180) {
            throw new IllegalArgumentException("Invalid longitude");
        }
    }


    private DriverProfile findDriverByUserId(UUID userId) {

        return driverProfileRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new DriverProfileNotFoundException("Driver profile not found"));
    }


    private DriverProfileResponse mapToResponse(DriverProfile driver) {

        return DriverProfileResponse.builder()
                .id(driver.getId())
                .userId(driver.getUserId())
                .licenseNumber(driver.getLicenseNumber())
                .driverStatus(driver.getStatus())
                .currentLat(driver.getCurrentLat())
                .currentLng(driver.getCurrentLng())
                .totalRides(driver.getTotalRides())
                .createdAt(driver.getCreatedAt())
                .build();
    }

    private VehicleResponse mapVehicle(Vehicle vehicle) {

        if (vehicle == null) {
            return null;
        }

        return VehicleResponse.builder()
                .id(vehicle.getId())
                .plateNumber(vehicle.getPlateNumber())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .color(vehicle.getColor())
                .year(vehicle.getYear())
                .build();
    }

    private String formatStatus(DriverStatus status) {
        return switch (status) {
            case OFFLINE -> "Offline";
            case AVAILABLE -> "Available";
            case ON_RIDE -> "On Ride";
            case BUSY -> "Busy";

        };

    }

}