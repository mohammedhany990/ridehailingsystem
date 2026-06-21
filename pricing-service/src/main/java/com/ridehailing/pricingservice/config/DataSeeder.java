package com.ridehailing.pricingservice.config;

import com.ridehailing.pricingservice.entity.FareRule;
import com.ridehailing.pricingservice.repository.FareRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds default fare rules on first startup if none exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final FareRuleRepository fareRuleRepository;

    @Override
    public void run(String... args) {
        if (fareRuleRepository.count() == 0) {
            log.info("Seeding default fare rules...");
            fareRuleRepository.saveAll(List.of(
                FareRule.builder()
                    .vehicleType("STANDARD")
                    .baseFare(new BigDecimal("10.00"))
                    .ratePerKm(new BigDecimal("5.00"))
                    .ratePerMin(new BigDecimal("0.75"))
                    .minimumFare(new BigDecimal("15.00"))
                    .currency("EGP")
                    .active(true)
                    .build(),
                FareRule.builder()
                    .vehicleType("PREMIUM")
                    .baseFare(new BigDecimal("20.00"))
                    .ratePerKm(new BigDecimal("9.00"))
                    .ratePerMin(new BigDecimal("1.50"))
                    .minimumFare(new BigDecimal("30.00"))
                    .currency("EGP")
                    .active(true)
                    .build(),
                FareRule.builder()
                    .vehicleType("MOTO")
                    .baseFare(new BigDecimal("5.00"))
                    .ratePerKm(new BigDecimal("3.00"))
                    .ratePerMin(new BigDecimal("0.50"))
                    .minimumFare(new BigDecimal("8.00"))
                    .currency("EGP")
                    .active(true)
                    .build()
            ));
            log.info("Default fare rules seeded.");
        }
    }
}
