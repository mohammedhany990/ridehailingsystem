package com.ridehailing.rideservice.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreadcrumbPoint {

    private Double lat;
    private Double lng;
    private Instant recordedAt;
}
