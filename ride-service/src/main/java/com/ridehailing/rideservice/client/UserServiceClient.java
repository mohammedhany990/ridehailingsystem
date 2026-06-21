package com.ridehailing.rideservice.client;

import com.ridehailing.rideservice.client.dto.UserApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${feign.client.config.user-service.url}")
public interface UserServiceClient {

    @PutMapping("/api/drivers/{userId}/start-ride")
    UserApiResponse<String> startRide(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("userId") UUID userId
    );

    @PutMapping("/api/drivers/{userId}/finish-ride")
    UserApiResponse<String> finishRide(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("userId") UUID userId
    );
}
