package com.ridehailing.mapservice.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class RouteResponse {

    private RouteInfo route;
}
