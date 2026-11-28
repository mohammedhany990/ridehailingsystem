package com.ridehailing.paymentservice.service;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@ConditionalOnProperty(name = "stripe.enabled", havingValue = "true")
public class StripeGatewayImpl implements StripeGateway {

    public StripeGatewayImpl(@Value("${stripe.secret-key}") String secretKey) {
        Stripe.apiKey = secretKey;
    }

    @Override
    public GatewayIntent createPaymentIntent(BigDecimal amount, String currency, String idempotencyKey) {
        try {
            long amountInMinor = amount.movePointRight(2).longValueExact();
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInMinor)
                    .setCurrency(currency.toLowerCase())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();
            PaymentIntent intent = PaymentIntent.create(
                    params,
                    RequestOptions.builder().setIdempotencyKey(idempotencyKey).build()
            );
            return new GatewayIntent(intent.getId(), intent.getClientSecret());
        } catch (Exception e) {
            throw new IllegalStateException("Stripe PaymentIntent creation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String retrieveStatus(String paymentIntentId) {
        try {
            return PaymentIntent.retrieve(paymentIntentId).getStatus();
        } catch (Exception e) {
            throw new IllegalStateException("Stripe retrieve failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean enabled() {
        return true;
    }
}
