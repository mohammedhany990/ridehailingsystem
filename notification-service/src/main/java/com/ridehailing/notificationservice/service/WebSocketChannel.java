package com.ridehailing.notificationservice.service;

import com.ridehailing.notificationservice.dto.NotificationResponse;
import com.ridehailing.notificationservice.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChannel implements NotificationChannel {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void deliver(Notification notification) {
        messagingTemplate.convertAndSend(
                "/topic/notifications." + notification.getUserId(),
                NotificationResponse.from(notification)
        );
    }
}
