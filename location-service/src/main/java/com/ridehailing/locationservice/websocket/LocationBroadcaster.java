package com.ridehailing.locationservice.websocket;

import com.ridehailing.locationservice.dto.LocationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcast(LocationResponse location) {
        String driverTopic = "/topic/drivers/" + location.getDriverId() + "/location";
        String globalTopic  = "/topic/drivers/location";

        try {
            messagingTemplate.convertAndSend(driverTopic, location);
            messagingTemplate.convertAndSend(globalTopic, location);
        } catch (Exception e) {
            log.warn("WebSocket broadcast failed for driver {}: {}", location.getDriverId(), e.getMessage());
        }
    }
}
