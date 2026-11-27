package com.ridehailing.notificationservice.service;

import com.ridehailing.notificationservice.dto.NotificationResponse;
import com.ridehailing.notificationservice.entity.Notification;
import com.ridehailing.notificationservice.entity.NotificationType;
import com.ridehailing.notificationservice.event.PaymentEvent;
import com.ridehailing.notificationservice.event.RideEvent;
import com.ridehailing.notificationservice.exception.NotificationNotFoundException;
import com.ridehailing.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;
    private final List<NotificationChannel> channels;

    @Transactional
    public List<Notification> handleRideEvent(RideEvent event) {
        NotificationType type = NotificationType.valueOf(event.eventType());
        List<Notification> created = new ArrayList<>();

        created.add(build(event.riderId(), type, riderTitle(type), riderBody(type, event), event));
        if (event.driverId() != null) {
            created.add(build(event.driverId(), type, driverTitle(type), driverBody(type, event), event));
        }

        List<Notification> saved = repository.saveAll(created);
        saved.forEach(n -> channels.forEach(c -> c.deliver(n)));
        return saved;
    }

    @Transactional
    public List<Notification> handlePaymentEvent(PaymentEvent event) {
        NotificationType type = NotificationType.valueOf(event.eventType());
        boolean success = type == NotificationType.PAYMENT_SUCCEEDED;
        String title = success ? "Payment successful" : "Payment failed";
        String amountText = event.amount() != null ? event.amount() + " " + event.currency() : "";
        String riderBodyText = success
                ? "Your payment of " + amountText + " for this ride went through."
                : "Your payment for this ride failed." + (event.failureReason() != null ? " Reason: " + event.failureReason() : "");
        String driverBodyText = success
                ? "The rider paid " + amountText + " for this ride."
                : "The rider's payment for this ride failed.";

        List<Notification> created = new ArrayList<>();
        created.add(Notification.builder()
                .id(UUID.randomUUID()).userId(event.riderId()).type(type)
                .title(title).body(riderBodyText).rideId(event.rideId())
                .read(false).createdAt(Instant.now()).build());
        if (event.driverId() != null) {
            created.add(Notification.builder()
                    .id(UUID.randomUUID()).userId(event.driverId()).type(type)
                    .title(title).body(driverBodyText).rideId(event.rideId())
                    .read(false).createdAt(Instant.now()).build());
        }
        List<Notification> saved = repository.saveAll(created);
        saved.forEach(n -> channels.forEach(c -> c.deliver(n)));
        return saved;
    }

    public Page<NotificationResponse> getForUser(UUID userId, Pageable pageable) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::from);
    }

    public long unreadCount(UUID userId) {
        return repository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(UUID id, UUID userId) {
        Notification n = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found: " + id));
        n.setRead(true);
        repository.save(n);
    }

    @Transactional
    public long markAllRead(UUID userId) {
        Page<Notification> unread = repository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged());
        long count = unread.stream().filter(n -> !n.isRead()).peek(n -> n.setRead(true)).count();
        repository.saveAll(unread.getContent());
        return count;
    }

    private Notification build(UUID userId, NotificationType type, String title, String body, RideEvent event) {
        return Notification.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .type(type)
                .title(title)
                .body(body)
                .rideId(event.rideId())
                .read(false)
                .createdAt(Instant.now())
                .build();
    }

    private String riderTitle(NotificationType type) {
        return switch (type) {
            case RIDE_REQUESTED -> "Ride requested";
            case RIDE_ACCEPTED -> "Driver on the way";
            case RIDE_STARTED -> "Your ride has started";
            case RIDE_COMPLETED -> "Ride completed";
            case RIDE_CANCELLED -> "Ride cancelled";
            case FARE_OFFERED -> "New fare offer";
            case FARE_OFFER_ACCEPTED -> "Fare agreed";
            case FARE_OFFER_DECLINED -> "Offer declined";
            case FARE_COUNTERED -> "Fare updated";
            default -> "Ride update";
        };
    }

    private String riderBody(NotificationType type, RideEvent event) {
        return switch (type) {
            case RIDE_REQUESTED -> "We received your ride request and are finding you a driver.";
            case RIDE_ACCEPTED -> "Your driver accepted the ride and is heading to your pickup point.";
            case RIDE_STARTED -> "Your ride is in progress. Enjoy the trip.";
            case RIDE_COMPLETED -> "Your ride is complete. Your fare has been calculated.";
            case RIDE_CANCELLED -> "Your ride was cancelled." + (event.reason() != null ? " Reason: " + event.reason() : "");
            case FARE_OFFERED -> "Your driver offers a fare of " + (event.reason() != null ? event.reason() : "") + " EGP. Accept or decline it.";
            case FARE_OFFER_ACCEPTED -> "You agreed on a fare of " + (event.reason() != null ? event.reason() : "") + " EGP.";
            case FARE_OFFER_DECLINED -> "You declined the driver's fare offer.";
            case FARE_COUNTERED -> "You updated your proposed fare to " + (event.reason() != null ? event.reason() : "") + " EGP.";
            default -> "Your ride status changed to " + type;
        };
    }

    private String driverTitle(NotificationType type) {
        return switch (type) {
            case RIDE_REQUESTED -> "New ride request";
            case RIDE_ACCEPTED -> "Ride assigned to you";
            case RIDE_STARTED -> "Ride in progress";
            case RIDE_COMPLETED -> "Ride completed";
            case RIDE_CANCELLED -> "Ride cancelled";
            case FARE_OFFERED -> "Fare offer sent";
            case FARE_OFFER_ACCEPTED -> "Your offer was accepted";
            case FARE_OFFER_DECLINED -> "Your offer was declined";
            case FARE_COUNTERED -> "Rider countered";
            default -> "Ride update";
        };
    }

    private String driverBody(NotificationType type, RideEvent event) {
        return switch (type) {
            case RIDE_REQUESTED -> "A rider near you requested a ride. Open the app to accept.";
            case RIDE_ACCEPTED -> "You accepted this ride. Head to the pickup point.";
            case RIDE_STARTED -> "You started this ride.";
            case RIDE_COMPLETED -> "You completed this ride. The fare is available in your history.";
            case RIDE_CANCELLED -> "This ride was cancelled." + (event.reason() != null ? " Reason: " + event.reason() : "");
            case FARE_OFFERED -> "You offered a fare of " + (event.reason() != null ? event.reason() : "") + " EGP for this ride.";
            case FARE_OFFER_ACCEPTED -> "The rider accepted your fare of " + (event.reason() != null ? event.reason() : "") + " EGP. Head to pickup.";
            case FARE_OFFER_DECLINED -> "The rider declined your fare offer.";
            case FARE_COUNTERED -> "The rider now proposes " + (event.reason() != null ? event.reason() : "") + " EGP. Accept or make a new offer.";
            default -> "Ride status changed to " + type;
        };
    }
}
