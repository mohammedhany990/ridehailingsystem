package com.ridehailing.paymentservice.kafka;

import com.ridehailing.paymentservice.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Value("${app.kafka.payment-events-topic:payment-events}")
    private String topic;

    public void publish(PaymentEvent event) {
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
