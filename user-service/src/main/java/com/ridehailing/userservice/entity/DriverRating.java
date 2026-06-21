package com.ridehailing.userservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
        name = "driver_ratings",
        indexes = {
                @Index(name = "idx_driver_rating_driver", columnList = "driver_id"),
                @Index(name = "idx_driver_rating_ride", columnList = "ride_id")
        }
)
public class DriverRating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Column(name = "passenger_id", nullable = false)
    private UUID passengerId;

    @Column(name = "ride_id", nullable = false, unique = true)
    private UUID rideId;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer rating;

    @Column(length = 500)
    private String comment;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}