package com.ridehailing.mapservice.service;


import com.ridehailing.mapservice.client.OsrmClient;
import com.ridehailing.mapservice.dto.OsrmResponse;
import com.ridehailing.mapservice.dto.RouteInfo;
import com.ridehailing.mapservice.dto.RouteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapService {

    private final OsrmClient osrmClient;

    public RouteInfo calculateRoute( RouteRequest request) {

        OsrmResponse response =
                osrmClient.getRoute(
                        request.getPickupLat(),
                        request.getPickupLng(),
                        request.getDropoffLat(),
                        request.getDropoffLng()
                );

        OsrmResponse.Route route =
                response.getRoutes().get(0);

        return RouteInfo.builder()
                .distanceKm(route.getDistance() / 1000.0)
                .durationMinutes(route.getDuration() / 60.0)
                .build();
    }
}