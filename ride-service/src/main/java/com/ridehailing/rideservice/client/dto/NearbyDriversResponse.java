package com.ridehailing.rideservice.client.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyDriversResponse {
    private String driverId;
    private Double distanceKm;
    private Double latitude;
    private Double longitude;
    private Double speedKmh;
    private Double bearing;
}
