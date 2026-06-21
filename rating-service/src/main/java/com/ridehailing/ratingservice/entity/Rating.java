package com.ridehailing.ratingservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ratings",
        indexes = {
                @Index(name = "idx_rating_ride_id", columnList = "rideId"),
                @Index(name = "idx_rating_ratee_id", columnList = "rateeId"),
                @Index(name = "idx_rating_rater_id", columnList = "raterId")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID rideId;

    @Column(nullable = false)
    private UUID raterId;

    @Column(nullable = false)
    private UUID rateeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartyType raterType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartyType rateeType;

    @Column(nullable = false)
    private Integer score;

    @Column(length = 500)
    private String comment;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}