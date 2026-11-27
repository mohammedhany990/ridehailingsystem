package com.ridehailing.paymentservice.repository;

import com.ridehailing.paymentservice.entity.DriverEarningsDaily;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverEarningsDailyRepository extends JpaRepository<DriverEarningsDaily, UUID> {

    Optional<DriverEarningsDaily> findByDriverIdAndEarningDate(UUID driverId, LocalDate earningDate);

    List<DriverEarningsDaily> findByDriverIdOrderByEarningDateDesc(UUID driverId);
}
