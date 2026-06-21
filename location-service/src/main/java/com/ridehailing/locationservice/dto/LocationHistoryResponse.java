package com.ridehailing.locationservice.dto;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationHistoryResponse {

    private double latitude;
    private double longitude;
    private Double speedKmh;
    private Double bearing;
    private Instant createdAt;
}
