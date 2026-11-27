package com.ridehailing.rideservice.kafka;

import com.ridehailing.rideservice.event.RideEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideEventPublisher {

    private final KafkaTemplate<String, RideEvent> kafkaTemplate;

    @Value("${app.kafka.ride-events-topic:ride-events}")
    private String topic;

    public void publish(RideEvent event) {
        kafkaTemplate.send(topic, event.rideId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish {} for ride {}", event.eventType(), event.rideId(), ex);
                    } else {
                        log.info("Published {} for ride {}", event.eventType(), event.rideId());
                    }
                });
    }
}
