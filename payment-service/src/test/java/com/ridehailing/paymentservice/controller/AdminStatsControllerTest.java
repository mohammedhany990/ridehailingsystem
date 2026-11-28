package com.ridehailing.paymentservice.controller;

import com.ridehailing.paymentservice.dto.AdminStatsResponse;
import com.ridehailing.paymentservice.entity.PaymentStatus;
import com.ridehailing.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminStatsControllerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private AdminStatsController controller;

    @Test
    void statsAggregatesRevenue() {
        when(paymentRepository.count()).thenReturn(30L);
        when(paymentRepository.countByStatus(PaymentStatus.SUCCEEDED)).thenReturn(25L);
        when(paymentRepository.countByStatus(PaymentStatus.FAILED)).thenReturn(3L);
        when(paymentRepository.sumSucceededAmounts()).thenReturn(new BigDecimal("4200.50"));
        when(paymentRepository.sumSucceededAmountsSince(any(Instant.class))).thenReturn(new BigDecimal("320.00"));

        var response = controller.stats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AdminStatsResponse stats = response.getBody().data();
        assertEquals(30L, stats.totalPayments());
        assertEquals(25L, stats.succeededPayments());
        assertEquals(new BigDecimal("4200.50"), stats.totalRevenue());
        assertEquals(new BigDecimal("320.00"), stats.revenueToday());
        assertEquals("EGP", stats.currency());
    }

    @Test
    void nullSumsBecomeZero() {
        when(paymentRepository.count()).thenReturn(0L);
        when(paymentRepository.countByStatus(PaymentStatus.SUCCEEDED)).thenReturn(0L);
        when(paymentRepository.countByStatus(PaymentStatus.FAILED)).thenReturn(0L);
        when(paymentRepository.sumSucceededAmounts()).thenReturn(null);
        when(paymentRepository.sumSucceededAmountsSince(any(Instant.class))).thenReturn(null);

        AdminStatsResponse stats = controller.stats().getBody().data();

        assertEquals(BigDecimal.ZERO, stats.totalRevenue());
        assertEquals(BigDecimal.ZERO, stats.revenueToday());
    }
}
