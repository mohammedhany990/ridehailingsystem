package com.ridehailing.notificationservice.kafka;

import com.ridehailing.notificationservice.entity.Notification;
import com.ridehailing.notificationservice.event.RideEvent;
import com.ridehailing.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = {"ride-events"})
@TestPropertySource(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
class RideEventListenerTest {

    @Autowired
    private KafkaTemplate<String, RideEvent> kafkaTemplate;

    @Autowired
    private NotificationRepository repository;

    @Test
    void consumesRideEventAndPersistsNotifications() {
        UUID rideId = UUID.randomUUID();
        UUID rider = UUID.randomUUID();
        UUID driver = UUID.randomUUID();
        RideEvent event = new RideEvent("RIDE_COMPLETED", rideId, rider, driver, null, Instant.now());

        kafkaTemplate.send("ride-events", rideId.toString(), event);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            List<Notification> all = repository.findAll();
            assertThat(all).hasSize(2);
            assertThat(all).extracting(Notification::getRideId).containsOnly(rideId);
        });
    }
}
