package com.ridehailing.notificationservice.dto;

import com.ridehailing.notificationservice.entity.Notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        String title,
        String body,
        UUID rideId,
        boolean read,
        Instant createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType().name(),
                n.getTitle(),
                n.getBody(),
                n.getRideId(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
