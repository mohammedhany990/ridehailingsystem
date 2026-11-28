package com.ridehailing.paymentservice.service;

import java.math.BigDecimal;

public interface StripeGateway {

    GatewayIntent createPaymentIntent(BigDecimal amount, String currency, String idempotencyKey);

    String retrieveStatus(String paymentIntentId);

    boolean enabled();
}
