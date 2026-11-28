package com.ridehailing.paymentservice.service;

import com.ridehailing.paymentservice.entity.Payment;
import com.ridehailing.paymentservice.entity.PaymentStatus;
import com.ridehailing.paymentservice.event.PaymentEvent;
import com.ridehailing.paymentservice.event.RideEvent;
import com.ridehailing.paymentservice.exception.PaymentNotFoundException;
import com.ridehailing.paymentservice.kafka.PaymentEventPublisher;
import com.ridehailing.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository repository;

    @Mock
    private StripeGateway stripeGateway;

    @Mock
    private PaymentEventPublisher publisher;

    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(repository, stripeGateway, publisher);
        ReflectionTestUtils.setField(service, "defaultCurrency", "EGP");
        ReflectionTestUtils.setField(service, "defaultAmount", new BigDecimal("85.50"));
    }

    private RideEvent completedRide(UUID rideId, UUID rider, UUID driver) {
        return new RideEvent("RIDE_COMPLETED", rideId, rider, driver, null, Instant.now());
    }

    @Test
    void rideCompletedCreatesPendingPaymentOnce() {
        UUID rideId = UUID.randomUUID();
        when(repository.findByRideId(rideId)).thenReturn(Optional.empty());
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment first = service.handleRideCompleted(completedRide(rideId, UUID.randomUUID(), UUID.randomUUID()));

        assertThat(first.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(first.getAmount()).isEqualByComparingTo("85.50");

        when(repository.findByRideId(rideId)).thenReturn(Optional.of(first));
        Payment second = service.handleRideCompleted(completedRide(rideId, UUID.randomUUID(), UUID.randomUUID()));

        assertThat(second.getId()).isEqualTo(first.getId());
        verify(repository, org.mockito.Mockito.times(1)).save(any(Payment.class));
    }

    @Test
    void chargeCreatesStripeIntentWithIdempotencyKey() {
        UUID rider = UUID.randomUUID();
        Payment pending = Payment.builder()
                .id(UUID.randomUUID()).rideId(UUID.randomUUID()).riderId(rider)
                .amount(new BigDecimal("85.50")).currency("EGP")
                .status(PaymentStatus.PENDING).createdAt(Instant.now()).build();

        when(repository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(stripeGateway.createPaymentIntent(any(), anyString(), anyString()))
                .thenReturn(new GatewayIntent("pi_test_123", "pi_test_123_secret_abc"));
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentService.ChargeResult charge = service.initiateCharge(pending.getId(), rider);
        Payment result = charge.payment();

        assertThat(result.getStripePaymentIntentId()).isEqualTo("pi_test_123");
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.REQUIRES_CONFIRMATION);
        assertThat(charge.clientSecret()).isEqualTo("pi_test_123_secret_abc");
        verify(stripeGateway).createPaymentIntent(
                new BigDecimal("85.50"), "EGP", "ride-" + pending.getRideId());
    }

    @Test
    void chargeRejectsNonChargeableState() {
        UUID rider = UUID.randomUUID();
        Payment done = Payment.builder()
                .id(UUID.randomUUID()).rideId(UUID.randomUUID()).riderId(rider)
                .amount(BigDecimal.TEN).currency("EGP")
                .status(PaymentStatus.SUCCEEDED).createdAt(Instant.now()).build();

        when(repository.findById(done.getId())).thenReturn(Optional.of(done));

        assertThatThrownBy(() -> service.initiateCharge(done.getId(), rider))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void webhookSuccessPublishesPaymentEvent() {
        Payment p = Payment.builder()
                .id(UUID.randomUUID()).rideId(UUID.randomUUID()).riderId(UUID.randomUUID())
                .driverId(UUID.randomUUID()).amount(BigDecimal.TEN).currency("EGP")
                .status(PaymentStatus.REQUIRES_CONFIRMATION)
                .stripePaymentIntentId("pi_test_9").createdAt(Instant.now()).build();

        when(repository.findByStripePaymentIntentId("pi_test_9")).thenReturn(Optional.of(p));
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = service.syncStatus("pi_test_9", "succeeded");

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo("PAYMENT_SUCCEEDED");
    }

    @Test
    void syncStatusIsQuietWhenNothingChanged() {
        Payment p = Payment.builder()
                .id(UUID.randomUUID()).rideId(UUID.randomUUID()).riderId(UUID.randomUUID())
                .amount(BigDecimal.TEN).currency("EGP")
                .status(PaymentStatus.SUCCEEDED)
                .stripePaymentIntentId("pi_test_8").createdAt(Instant.now()).build();

        when(repository.findByStripePaymentIntentId("pi_test_8")).thenReturn(Optional.of(p));

        service.syncStatus("pi_test_8", "succeeded");

        verify(publisher, never()).publish(any());
    }

    @Test
    void foreignUserCannotAccessPayment() {
        Payment p = Payment.builder()
                .id(UUID.randomUUID()).rideId(UUID.randomUUID()).riderId(UUID.randomUUID())
                .amount(BigDecimal.TEN).currency("EGP")
                .status(PaymentStatus.PENDING).createdAt(Instant.now()).build();

        when(repository.findById(p.getId())).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> service.getOwned(p.getId(), UUID.randomUUID()))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void simulatedConfirmPublishesSuccess() {
        UUID rider = UUID.randomUUID();
        Payment pending = Payment.builder()
                .id(UUID.randomUUID()).rideId(UUID.randomUUID()).riderId(rider)
                .amount(BigDecimal.TEN).currency("EGP")
                .status(PaymentStatus.PENDING).createdAt(Instant.now()).build();

        lenient().when(stripeGateway.enabled()).thenReturn(false);
        when(repository.findById(pending.getId())).thenReturn(Optional.of(pending));
        when(repository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = service.confirmSimulated(pending.getId(), rider);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        verify(publisher).publish(any(PaymentEvent.class));
    }
}
