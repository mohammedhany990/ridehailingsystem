package com.ridehailing.mapservice.controller;


import com.ridehailing.mapservice.dto.RouteInfo;
import com.ridehailing.mapservice.dto.RouteRequest;
import com.ridehailing.mapservice.dto.RouteResponse;
import com.ridehailing.mapservice.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maps")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    @PostMapping("/route")
    public RouteResponse route(
            @RequestBody RouteRequest request
    ) {

        RouteInfo info = mapService.calculateRoute(request);

        return RouteResponse.builder()
                .route(info)
                .build();
    }
}