package com.ridehailing.paymentservice.kafka;

import com.ridehailing.paymentservice.event.RideEvent;
import com.ridehailing.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RideEventListener {

    private final PaymentService paymentService;

    @KafkaListener(topics = "${app.kafka.ride-events-topic:ride-events}", groupId = "payment-service")
    public void onRideEvent(RideEvent event) {
        if ("RIDE_COMPLETED".equals(event.eventType())) {
            log.info("Creating pending payment for completed ride {}", event.rideId());
            paymentService.handleRideCompleted(event);
        }
    }
}
