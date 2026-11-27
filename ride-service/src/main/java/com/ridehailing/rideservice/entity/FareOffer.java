package com.ridehailing.rideservice.entity;

import jakarta.persistence.*;
import com.ridehailing.rideservice.exception.InvalidRideStateException;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fare_offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    @Column(nullable = false)
    private UUID driverId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "EGP";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FareOfferStatus status = FareOfferStatus.PENDING;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public void accept() {
        assertPending();
        this.status = FareOfferStatus.ACCEPTED;
    }

    public void decline() {
        assertPending();
        this.status = FareOfferStatus.DECLINED;
    }

    public void supersede() {
        if (this.status == FareOfferStatus.PENDING) {
            this.status = FareOfferStatus.SUPERSEDED;
        }
    }

    public void expire() {
        if (this.status == FareOfferStatus.PENDING) {
            this.status = FareOfferStatus.EXPIRED;
        }
    }

    private void assertPending() {
        if (this.status != FareOfferStatus.PENDING) {
            throw new InvalidRideStateException(
                    "Offer is " + this.status + " and can no longer change"
            );
        }
    }
}
