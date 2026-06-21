package com.ridehailing.userservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "vehicles",
        indexes = {
                @Index(name = "idx_vehicle_plate", columnList = "plate_number"),
                @Index(name = "idx_vehicle_driver", columnList = "driver_profile_id")
        }
)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_profile_id", nullable = false, unique = true)
    private DriverProfile driverProfile;

    @NotBlank
    @Column(name = "plate_number", nullable = false, unique = true)
    private String plateNumber;

    @Column(nullable = false)
    private String make;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String color;

    @Min(1980)
    @Max(2100)
    private Integer year;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}