package com.ridehailing.rideservice.client;

import com.ridehailing.rideservice.client.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${feign.client.config.notification-service.url:http://localhost:8088}")
public interface NotificationClient {
    @PostMapping("/api/notifications/send")
    void sendNotification(@RequestBody NotificationRequest request);
}