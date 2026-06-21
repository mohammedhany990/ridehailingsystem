package com.ridehailing.rideservice.client;

import com.ridehailing.rideservice.client.dto.RouteRequest;
import com.ridehailing.rideservice.client.dto.RouteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "map-service", url = "${feign.client.config.map-service.url}")
public interface MapClient {

    @PostMapping("/api/maps/route")
    RouteResponse route(
            @RequestBody RouteRequest request
    );
}