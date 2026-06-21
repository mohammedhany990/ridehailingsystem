package com.ridehailing.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "driver_profiles",
        indexes = {
                @Index(name = "idx_driver_user_id", columnList = "userId"),
                @Index(name = "idx_driver_status", columnList = "status")
        }
)@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "license_number", nullable = false, unique = true)
    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DriverStatus status = DriverStatus.OFFLINE;

    @Column(name = "current_lat")
    private Double currentLat;

    @Column(name = "current_lng")
    private Double currentLng;

    @Column(name = "location_updated_at")
    private LocalDateTime locationUpdatedAt;

    @Column(nullable = false)
    @Builder.Default
    private Double rating = 5.0;

    @Column(name = "total_ratings", nullable = false)
    @Builder.Default
    private Integer totalRatings = 0;

    @Column(name = "total_rides")
    @Builder.Default
    private Integer totalRides = 0;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // One-to-one with Vehicle
    @OneToOne(
            mappedBy = "driverProfile",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Vehicle vehicle;

    public void goOnline() {
        this.status = DriverStatus.AVAILABLE;
    }

    public void goOffline() {
        this.status = DriverStatus.OFFLINE;
    }

    public void startRide() {
        this.status = DriverStatus.ON_RIDE;
    }

    public void finishRide() {
        this.status = DriverStatus.AVAILABLE;
        this.totalRides++;
    }

    public void updateLocation(Double lat, Double lng) {
        this.currentLat = lat;
        this.currentLng = lng;
        this.locationUpdatedAt = LocalDateTime.now();
    }
}
