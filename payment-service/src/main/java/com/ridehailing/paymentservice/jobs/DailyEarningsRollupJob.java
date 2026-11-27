package com.ridehailing.paymentservice.jobs;

import com.ridehailing.paymentservice.entity.DriverEarningsDaily;
import com.ridehailing.paymentservice.entity.Payment;
import com.ridehailing.paymentservice.entity.PaymentStatus;
import com.ridehailing.paymentservice.repository.DriverEarningsDailyRepository;
import com.ridehailing.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyEarningsRollupJob {

    private final PaymentRepository paymentRepository;
    private final DriverEarningsDailyRepository earningsRepository;

    @Scheduled(cron = "${app.jobs.earnings-rollup-cron:0 5 0 * * *}")
    @SchedulerLock(name = "dailyEarningsRollup", lockAtLeastFor = "PT30S", lockAtMostFor = "PT10M")
    @Transactional
    public void rollupYesterday() {
        rollup(LocalDate.now(ZoneOffset.UTC).minusDays(1));
    }

    public int rollup(LocalDate date) {
        Instant from = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<Payment> succeeded = paymentRepository
                .findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        PaymentStatus.SUCCEEDED, from, to);

        Map<UUID, List<Payment>> byDriver = succeeded.stream()
                .filter(p -> p.getDriverId() != null)
                .collect(Collectors.groupingBy(Payment::getDriverId));

        byDriver.forEach((driverId, payments) -> {
            BigDecimal total = payments.stream()
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            DriverEarningsDaily row = earningsRepository
                    .findByDriverIdAndEarningDate(driverId, date)
                    .orElse(DriverEarningsDaily.builder()
                            .driverId(driverId)
                            .earningDate(date)
                            .currency(payments.get(0).getCurrency() != null
                                    ? payments.get(0).getCurrency() : "EGP")
                            .build());
            row.setTotalAmount(total);
            row.setRideCount(payments.size());
            earningsRepository.save(row);
        });

        log.info("Earnings rollup for {}: {} drivers, {} payments", date, byDriver.size(), succeeded.size());
        return byDriver.size();
    }
}
