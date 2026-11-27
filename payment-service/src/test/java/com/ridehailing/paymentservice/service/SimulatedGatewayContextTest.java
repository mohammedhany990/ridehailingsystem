package com.ridehailing.paymentservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SimulatedGatewayContextTest {

    @Autowired
    private StripeGateway stripeGateway;

    @Test
    void simulatedGatewayIsActiveWithoutStripeKey() {
        assertThat(stripeGateway).isInstanceOf(SimulatedStripeGateway.class);
        assertThat(stripeGateway.enabled()).isFalse();
    }
}
