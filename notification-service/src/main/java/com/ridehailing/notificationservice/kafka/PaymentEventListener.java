package com.ridehailing.notificationservice.kafka;

import com.ridehailing.notificationservice.event.PaymentEvent;
import com.ridehailing.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${app.kafka.payment-events-topic:payment-events}", groupId = "notification-service",
            properties = "spring.json.value.default.type=com.ridehailing.notificationservice.event.PaymentEvent")
    public void onPaymentEvent(PaymentEvent event) {
        log.info("Received payment event {} for ride {}", event.eventType(), event.rideId());
        notificationService.handlePaymentEvent(event);
    }
}
