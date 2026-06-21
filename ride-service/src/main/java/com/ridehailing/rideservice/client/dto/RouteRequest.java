package com.ridehailing.rideservice.client.dto;


import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {

    private double pickupLat;
    private double pickupLng;

    private double dropoffLat;
    private double dropoffLng;
}
