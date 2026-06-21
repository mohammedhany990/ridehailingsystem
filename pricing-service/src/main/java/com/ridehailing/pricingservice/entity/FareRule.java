package com.ridehailing.pricingservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fare_rules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String vehicleType; // STANDARD, PREMIUM, MOTO

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseFare;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal ratePerKm;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal ratePerMin;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumFare;

    @Column(nullable = false)
    @Builder.Default
    private String currency = "EGP";

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}
