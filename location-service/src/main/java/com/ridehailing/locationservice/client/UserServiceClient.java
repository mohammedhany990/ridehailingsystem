package com.ridehailing.locationservice.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "user-service", url = "${feign.client.config.user-service.url}")
public interface UserServiceClient {

    @PostMapping("/api/drivers/mark-offline")
    void markDriversOffline(
            @RequestBody List<UUID> driverIds
    );
}