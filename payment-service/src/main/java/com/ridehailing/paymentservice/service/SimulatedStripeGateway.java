package com.ridehailing.paymentservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "stripe.enabled", havingValue = "false", matchIfMissing = true)
public class SimulatedStripeGateway implements StripeGateway {

    @Override
    public String createPaymentIntent(BigDecimal amount, String currency, String idempotencyKey) {
        return "sim_pi_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    }

    @Override
    public String retrieveStatus(String paymentIntentId) {
        return "succeeded";
    }

    @Override
    public boolean enabled() {
        return false;
    }
}
