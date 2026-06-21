package com.ridehailing.rideservice.entity;

import com.ridehailing.rideservice.exception.InvalidRideStateException;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "rides",
        indexes = {
                @Index(name = "idx_ride_rider_id", columnList = "riderId"),
                @Index(name = "idx_ride_driver_id", columnList = "driverId"),
                @Index(name = "idx_ride_status", columnList = "status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID riderId;

    private UUID driverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RideStatus status = RideStatus.REQUESTED;

    @Column(nullable = false)
    private Double pickupLat;

    @Column(nullable = false)
    private Double pickupLng;

    @Column(nullable = false)
    private Double dropoffLat;

    @Column(nullable = false)
    private Double dropoffLng;

    @Column(nullable = false)
    @Builder.Default
    private Instant requestedAt = Instant.now();

    private Instant acceptedAt;
    private Instant startedAt;
    private Instant completedAt;

    @OneToOne(mappedBy = "ride", cascade = CascadeType.ALL, orphanRemoval = true)
    private Fare fare;

    @OneToOne(mappedBy = "ride", cascade = CascadeType.ALL, orphanRemoval = true)
    private RideCancellation cancellation;

    public void accept(UUID driverId) {
        assertStatus(RideStatus.REQUESTED);
        this.driverId = driverId;
        this.status = RideStatus.ACCEPTED;
        this.acceptedAt = Instant.now();
    }

    public void start() {
        assertStatus(RideStatus.ACCEPTED);
        this.status = RideStatus.IN_PROGRESS;
        this.startedAt = Instant.now();
    }

    public void complete() {
        assertStatus(RideStatus.IN_PROGRESS);
        this.status = RideStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void cancel(String cancelledBy, String reason) {
        if (status == RideStatus.COMPLETED || status == RideStatus.CANCELLED) {
            throw new InvalidRideStateException("Cannot cancel a " + status + " ride");
        }
        this.status = RideStatus.CANCELLED;
        this.cancellation = RideCancellation.builder()
                .ride(this)
                .cancelledBy(cancelledBy)
                .reason(reason)
                .cancelledAt(Instant.now())
                .build();
    }

    private void assertStatus(RideStatus expected) {
        if (this.status != expected) {
            throw new InvalidRideStateException(
                    "Expected " + expected + " but was " + this.status
            );
        }
    }
}