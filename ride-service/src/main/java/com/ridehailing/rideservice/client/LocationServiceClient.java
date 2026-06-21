package com.ridehailing.rideservice.client;


import com.ridehailing.rideservice.client.dto.LocationApiResponse;
import com.ridehailing.rideservice.client.dto.NearbyDriversRequest;
import com.ridehailing.rideservice.client.dto.NearbyDriversResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "location-service", url = "${feign.client.config.location-service.url}")
public interface LocationServiceClient {

    @PostMapping("/api/location/nearby")
    LocationApiResponse<List<NearbyDriversResponse>> getNearbyDrivers(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody NearbyDriversRequest request
    );
}
