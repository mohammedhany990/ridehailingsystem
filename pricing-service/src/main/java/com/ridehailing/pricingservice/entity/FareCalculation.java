package com.ridehailing.pricingservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fare_calculations",
        indexes = @Index(name = "idx_fare_calc_ride_id", columnList = "rideId", unique = true))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID rideId;

    @Column(nullable = false)
    private String vehicleType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFare;

    @Column(nullable = false, precision = 8, scale = 3)
    private BigDecimal distanceKm;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal durationMinutes;

    @Column(nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal surgeMultiplier = BigDecimal.ONE;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal distanceCost;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal timeCost;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    @Builder.Default
    private String currency = "EGP";

    @Builder.Default
    private LocalDateTime calculatedAt = LocalDateTime.now();
}
