package com.ridehailing.userservice.dto;

import com.ridehailing.userservice.entity.DriverStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DriverProfileResponse {

        private UUID id;
        private UUID userId;
        private String licenseNumber;
        private DriverStatus driverStatus;

        private Double currentLat;
        private Double currentLng;

        private Integer totalRides;

        private LocalDateTime createdAt;

}