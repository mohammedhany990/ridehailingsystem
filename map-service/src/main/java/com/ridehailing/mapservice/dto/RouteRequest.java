package com.ridehailing.mapservice.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class RouteRequest {

    private double pickupLat;
    private double pickupLng;

    private double dropoffLat;
    private double dropoffLng;
}