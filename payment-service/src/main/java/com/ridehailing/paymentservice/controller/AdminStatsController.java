package com.ridehailing.paymentservice.controller;

import com.ridehailing.paymentservice.common.ApiResponse;
import com.ridehailing.paymentservice.dto.AdminStatsResponse;
import com.ridehailing.paymentservice.entity.PaymentStatus;
import com.ridehailing.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/payments/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final PaymentRepository paymentRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminStatsResponse>> stats() {
        Instant startOfToday = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        BigDecimal totalRevenue = paymentRepository.sumSucceededAmounts();
        BigDecimal revenueToday = paymentRepository.sumSucceededAmountsSince(startOfToday);
        AdminStatsResponse stats = new AdminStatsResponse(
                paymentRepository.count(),
                paymentRepository.countByStatus(PaymentStatus.SUCCEEDED),
                paymentRepository.countByStatus(PaymentStatus.FAILED),
                totalRevenue == null ? BigDecimal.ZERO : totalRevenue,
                revenueToday == null ? BigDecimal.ZERO : revenueToday,
                "EGP"
        );
        return ResponseEntity.ok(ApiResponse.success("Admin stats fetched", stats));
    }
}
