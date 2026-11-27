package com.ridehailing.notificationservice.service;

import com.ridehailing.notificationservice.entity.Notification;

public interface NotificationChannel {

    void deliver(Notification notification);
}
