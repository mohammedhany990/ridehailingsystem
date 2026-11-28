package com.ridehailing.paymentservice.dto;

import java.math.BigDecimal;

public record AdminStatsResponse(
        long totalPayments,
        long succeededPayments,
        long failedPayments,
        BigDecimal totalRevenue,
        BigDecimal revenueToday,
        String currency
) {
}
