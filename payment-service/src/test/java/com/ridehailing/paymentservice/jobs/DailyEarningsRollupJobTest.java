package com.ridehailing.paymentservice.jobs;

import com.ridehailing.paymentservice.entity.DriverEarningsDaily;
import com.ridehailing.paymentservice.entity.Payment;
import com.ridehailing.paymentservice.entity.PaymentStatus;
import com.ridehailing.paymentservice.repository.DriverEarningsDailyRepository;
import com.ridehailing.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyEarningsRollupJobTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private DriverEarningsDailyRepository earningsRepository;

    @InjectMocks
    private DailyEarningsRollupJob job;

    private Payment payment(UUID driverId, String amount) {
        return Payment.builder()
                .id(UUID.randomUUID())
                .driverId(driverId)
                .riderId(UUID.randomUUID())
                .rideId(UUID.randomUUID())
                .amount(new BigDecimal(amount))
                .currency("EGP")
                .status(PaymentStatus.SUCCEEDED)
                .build();
    }

    @Test
    void paymentsAreAggregatedPerDriver() {
        LocalDate date = LocalDate.of(2026, 11, 27);
        UUID driver = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        when(paymentRepository.findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                eq(PaymentStatus.SUCCEEDED), any(), any()))
                .thenReturn(List.of(
                        payment(driver, "50.00"), payment(driver, "30.00"), payment(other, "20.00")));
        when(earningsRepository.findByDriverIdAndEarningDate(any(), eq(date)))
                .thenReturn(Optional.empty());

        int drivers = job.rollup(date);

        assertThat(drivers).isEqualTo(2);
        ArgumentCaptor<DriverEarningsDaily> captor = ArgumentCaptor.forClass(DriverEarningsDaily.class);
        verify(earningsRepository, times(2)).save(captor.capture());
        DriverEarningsDaily row = captor.getAllValues().stream()
                .filter(r -> r.getDriverId().equals(driver)).findFirst().orElseThrow();
        assertThat(row.getTotalAmount()).isEqualByComparingTo("80.00");
        assertThat(row.getRideCount()).isEqualTo(2);
        assertThat(row.getEarningDate()).isEqualTo(date);
    }

    @Test
    void existingRowIsUpdatedNotDuplicated() {
        LocalDate date = LocalDate.of(2026, 11, 27);
        UUID driver = UUID.randomUUID();
        DriverEarningsDaily existing = DriverEarningsDaily.builder()
                .id(UUID.randomUUID()).driverId(driver).earningDate(date)
                .totalAmount(new BigDecimal("1.00")).rideCount(1).build();
        when(paymentRepository.findByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                eq(PaymentStatus.SUCCEEDED), any(), any()))
                .thenReturn(List.of(payment(driver, "45.00")));
        when(earningsRepository.findByDriverIdAndEarningDate(driver, date))
                .thenReturn(Optional.of(existing));

        job.rollup(date);

        assertThat(existing.getTotalAmount()).isEqualByComparingTo("45.00");
        assertThat(existing.getRideCount()).isEqualTo(1);
        verify(earningsRepository).save(existing);
    }
}
