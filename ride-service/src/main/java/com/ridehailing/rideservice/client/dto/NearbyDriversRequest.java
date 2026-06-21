package com.ridehailing.rideservice.client.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyDriversRequest {
    private Double  latitude;
    private Double  longitude;
    private Double  radiusKm;

    @Builder.Default
    private int     limit = 10;
    @Builder.Default
    private boolean includeDistance = true;
}
