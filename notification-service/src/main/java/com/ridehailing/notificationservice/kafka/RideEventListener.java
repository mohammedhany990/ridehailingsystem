package com.ridehailing.notificationservice.kafka;

import com.ridehailing.notificationservice.event.RideEvent;
import com.ridehailing.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${app.kafka.ride-events-topic:ride-events}", groupId = "notification-service",
            properties = "spring.json.value.default.type=com.ridehailing.notificationservice.event.RideEvent")
    public void onRideEvent(RideEvent event) {
        log.info("Received ride event {} for ride {}", event.eventType(), event.rideId());
        notificationService.handleRideEvent(event);
    }
}
