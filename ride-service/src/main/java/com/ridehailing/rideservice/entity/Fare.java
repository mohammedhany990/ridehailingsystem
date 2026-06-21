package com.ridehailing.rideservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fares")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fare {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false, unique = true)
    private Ride ride;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFare;

    @Column(nullable = false, precision = 8, scale = 3)
    private BigDecimal distanceKm;

    @Column(nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal surgeMultiplier = BigDecimal.ONE;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Builder.Default
    @Column(nullable = false)
    private String currency = "EGP";

    @Column(nullable = false)
    private Instant calculatedAt;

    @PrePersist
    public void prePersist() {
        if (calculatedAt == null) {
            calculatedAt = Instant.now();
        }
    }
}