package com.ridehailing.paymentservice.controller;

import com.ridehailing.paymentservice.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StripeWebhookControllerTest {

    private static final String SECRET = "whsec_test_secret_1234567890";
    private static final String PAYLOAD = """
            {
              "id": "evt_test_1",
              "object": "event",
              "api_version": "2025-05-28.basil",
              "type": "payment_intent.succeeded",
              "data": {
                "object": {
                  "id": "pi_test_123",
                  "object": "payment_intent",
                  "status": "succeeded",
                  "amount": 8550,
                  "currency": "egp"
                }
              }
            }
            """;

    private PaymentService paymentService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);
        StripeWebhookController controller = new StripeWebhookController(paymentService);
        ReflectionTestUtils.setField(controller, "webhookSecret", SECRET);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private String signatureFor(String payload, long timestamp) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signed = timestamp + "." + payload;
        String hex = HexFormat.of().formatHex(mac.doFinal(signed.getBytes(StandardCharsets.UTF_8)));
        return "t=" + timestamp + ",v1=" + hex;
    }

    @Test
    void validSignatureSyncsPaymentStatus() throws Exception {
        long t = Instant.now().getEpochSecond();

        mvc.perform(post("/api/payments/webhook")
                        .contentType("application/json")
                        .content(PAYLOAD)
                        .header("Stripe-Signature", signatureFor(PAYLOAD, t)))
                .andExpect(status().isOk());

        verify(paymentService).syncStatus("pi_test_123", "succeeded");
    }

    @Test
    void forgedSignatureIsRejected() throws Exception {
        mvc.perform(post("/api/payments/webhook")
                        .contentType("application/json")
                        .content(PAYLOAD)
                        .header("Stripe-Signature", "t=1,v1=forged"))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).syncStatus(anyString(), anyString());
    }

    @Test
    void tamperedPayloadIsRejected() throws Exception {
        long t = Instant.now().getEpochSecond();
        String validSignature = signatureFor(PAYLOAD, t);
        String tampered = PAYLOAD.replace("8550", "100");

        mvc.perform(post("/api/payments/webhook")
                        .contentType("application/json")
                        .content(tampered)
                        .header("Stripe-Signature", validSignature))
                .andExpect(status().isBadRequest());

        verify(paymentService, never()).syncStatus(anyString(), anyString());
    }
}
