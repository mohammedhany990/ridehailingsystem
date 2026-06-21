package com.ridehailing.userservice.dto;


import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverRatingResponse {

    private UUID id;

    private UUID driverId;

    private UUID passengerId;

    private UUID rideId;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;
}
