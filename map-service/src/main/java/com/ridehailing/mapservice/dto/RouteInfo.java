package com.ridehailing.mapservice.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RouteInfo {

    private double distanceKm;

    private double durationMinutes;
}