package com.ridehailing.notificationservice.service;

import com.ridehailing.notificationservice.entity.Notification;
import com.ridehailing.notificationservice.entity.NotificationType;
import com.ridehailing.notificationservice.event.RideEvent;
import com.ridehailing.notificationservice.exception.NotificationNotFoundException;
import com.ridehailing.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private NotificationChannel channel;

    @Test
    void rideEventCreatesNotificationsForRiderAndDriver() {
        NotificationService service = new NotificationService(repository, List.of(channel));
        UUID rider = UUID.randomUUID();
        UUID driver = UUID.randomUUID();
        RideEvent event = new RideEvent("RIDE_ACCEPTED", UUID.randomUUID(), rider, driver, null, Instant.now());

        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<Notification> result = service.handleRideEvent(event);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Notification::getUserId).containsExactlyInAnyOrder(rider, driver);
        assertThat(result).allMatch(n -> n.getType() == NotificationType.RIDE_ACCEPTED && !n.isRead());
        result.forEach(n -> verify(channel).deliver(n));
    }

    @Test
    void rideEventWithoutDriverNotifiesOnlyRider() {
        NotificationService service = new NotificationService(repository, List.of(channel));
        UUID rider = UUID.randomUUID();
        RideEvent event = new RideEvent("RIDE_REQUESTED", UUID.randomUUID(), rider, null, null, Instant.now());

        when(repository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<Notification> result = service.handleRideEvent(event);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(rider);
    }

    @Test
    void markReadSetsFlagOnOwnedNotification() {
        NotificationService service = new NotificationService(repository, List.of(channel));
        UUID user = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Notification n = Notification.builder()
                .id(id).userId(user).type(NotificationType.GENERAL)
                .title("t").body("b").read(false).createdAt(Instant.now()).build();

        when(repository.findByIdAndUserId(id, user)).thenReturn(Optional.of(n));

        service.markRead(id, user);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().isRead()).isTrue();
    }

    @Test
    void markReadRejectsForeignNotification() {
        NotificationService service = new NotificationService(repository, List.of(channel));
        UUID user = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        when(repository.findByIdAndUserId(id, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markRead(id, user))
                .isInstanceOf(NotificationNotFoundException.class);
    }
}
