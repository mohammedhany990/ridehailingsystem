package com.ridehailing.paymentservice.service;

import com.ridehailing.paymentservice.dto.PaymentResponse;
import com.ridehailing.paymentservice.dto.ReceiptResponse;
import com.ridehailing.paymentservice.entity.Payment;
import com.ridehailing.paymentservice.entity.PaymentStatus;
import com.ridehailing.paymentservice.event.PaymentEvent;
import com.ridehailing.paymentservice.event.RideEvent;
import com.ridehailing.paymentservice.exception.PaymentNotFoundException;
import com.ridehailing.paymentservice.kafka.PaymentEventPublisher;
import com.ridehailing.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repository;
    private final StripeGateway stripeGateway;
    private final PaymentEventPublisher publisher;

    @Value("${app.payments.currency:EGP}")
    private String defaultCurrency;

    @Value("${app.payments.default-amount:0}")
    private java.math.BigDecimal defaultAmount;

    @Value("${stripe.publishable-key:}")
    private String publishableKey;

    public boolean liveGateway() {
        return stripeGateway.enabled();
    }

    public String publishableKey() {
        return stripeGateway.enabled() ? publishableKey : null;
    }

    @Transactional
    public Payment handleRideCompleted(RideEvent event) {
        return repository.findByRideId(event.rideId())
                .map(existing -> {
                    log.info("Payment already exists for ride {}, skipping (idempotent)", event.rideId());
                    return existing;
                })
                .orElseGet(() -> createPending(event));
    }

    private Payment createPending(RideEvent event) {
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .rideId(event.rideId())
                .riderId(event.riderId())
                .driverId(event.driverId())
                .amount(defaultAmount)
                .currency(defaultCurrency)
                .status(PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        return repository.save(payment);
    }

    @Transactional
    public ChargeResult initiateCharge(UUID paymentId, UUID riderId) {
        Payment payment = findOwned(paymentId, riderId);
        if (payment.getStatus() != PaymentStatus.PENDING && payment.getStatus() != PaymentStatus.FAILED) {
            throw new IllegalStateException("Payment is not chargeable in state " + payment.getStatus());
        }
        GatewayIntent intent = stripeGateway.createPaymentIntent(
                payment.getAmount(), payment.getCurrency(), "ride-" + payment.getRideId());
        payment.setStripePaymentIntentId(intent.id());
        payment.setStatus(PaymentStatus.REQUIRES_CONFIRMATION);
        payment.setUpdatedAt(Instant.now());
        return new ChargeResult(repository.save(payment), intent.clientSecret(), stripeGateway.enabled());
    }

    public record ChargeResult(Payment payment, String clientSecret, boolean liveGateway) {
    }

    @Transactional
    public Payment syncStatus(String stripePaymentIntentId, String stripeStatus) {
        Payment payment = repository.findByStripePaymentIntentId(stripePaymentIntentId)
                .orElseThrow(() -> new PaymentNotFoundException("No payment for intent " + stripePaymentIntentId));
        PaymentStatus newStatus = switch (stripeStatus) {
            case "succeeded" -> PaymentStatus.SUCCEEDED;
            case "canceled", "payment_failed" -> PaymentStatus.FAILED;
            case "requires_payment_method" -> PaymentStatus.FAILED;
            default -> payment.getStatus();
        };
        if (newStatus != payment.getStatus()) {
            payment.setStatus(newStatus);
            payment.setUpdatedAt(Instant.now());
            Payment saved = repository.save(payment);
            String eventType = newStatus == PaymentStatus.SUCCEEDED ? "PAYMENT_SUCCEEDED" : "PAYMENT_FAILED";
            publisher.publish(PaymentEvent.of(
                    eventType, saved.getId(), saved.getRideId(), saved.getRiderId(), saved.getDriverId(),
                    saved.getAmount(), saved.getCurrency(), saved.getFailureReason()));
            return saved;
        }
        return payment;
    }

    @Transactional
    public Payment confirmSimulated(UUID paymentId, UUID riderId) {
        Payment payment = findOwned(paymentId, riderId);
        if (stripeGateway.enabled()) {
            throw new IllegalStateException("Simulated confirmation is only available without a live Stripe key");
        }
        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setUpdatedAt(Instant.now());
        Payment saved = repository.save(payment);
        publisher.publish(PaymentEvent.of(
                "PAYMENT_SUCCEEDED", saved.getId(), saved.getRideId(), saved.getRiderId(), saved.getDriverId(),
                saved.getAmount(), saved.getCurrency(), null));
        return saved;
    }

    @Transactional
    public Payment refreshFromGateway(UUID paymentId, UUID riderId) {
        if (!stripeGateway.enabled()) {
            throw new IllegalStateException("Status refresh is only available with a live Stripe key");
        }
        Payment payment = findOwned(paymentId, riderId);
        String intentId = payment.getStripePaymentIntentId();
        if (intentId == null || intentId.isBlank()) {
            throw new IllegalStateException("Payment has no Stripe intent yet");
        }
        return syncStatus(intentId, stripeGateway.retrieveStatus(intentId));
    }

    public Page<PaymentResponse> historyForRider(UUID riderId, Pageable pageable) {
        return repository.findByRiderIdOrderByCreatedAtDesc(riderId, pageable).map(PaymentResponse::from);
    }

    public Page<PaymentResponse> historyForDriver(UUID driverId, Pageable pageable) {
        return repository.findByDriverIdOrderByCreatedAtDesc(driverId, pageable).map(PaymentResponse::from);
    }

    public PaymentResponse getOwned(UUID paymentId, UUID userId) {
        return PaymentResponse.from(findOwned(paymentId, userId));
    }

    public ReceiptResponse receiptForRide(UUID rideId, UUID userId) {
        Payment payment = repository.findByRideId(rideId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for ride: " + rideId));
        if (!payment.getRiderId().equals(userId) && !userId.equals(payment.getDriverId())) {
            throw new PaymentNotFoundException("Payment not found for ride: " + rideId);
        }
        return ReceiptResponse.from(payment);
    }

    private Payment findOwned(UUID paymentId, UUID userId) {
        Payment payment = repository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
        if (!payment.getRiderId().equals(userId) && !userId.equals(payment.getDriverId())) {
            throw new PaymentNotFoundException("Payment not found: " + paymentId);
        }
        return payment;
    }
}
