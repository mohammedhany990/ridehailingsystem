package com.ridehailing.paymentservice.entity;

public enum PaymentStatus {
    PENDING,
    REQUIRES_CONFIRMATION,
    SUCCEEDED,
    FAILED,
    REFUNDED
}
