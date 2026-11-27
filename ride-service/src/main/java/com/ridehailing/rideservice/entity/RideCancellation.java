package com.ridehailing.rideservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ride_cancellations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideCancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false, unique = true)
    private Ride ride;

    @Column(nullable = false)
    private String cancelledBy;

    private String reason;

    @Column(nullable = false)
    private Instant cancelledAt;

    private BigDecimal cancellationFee;
}
