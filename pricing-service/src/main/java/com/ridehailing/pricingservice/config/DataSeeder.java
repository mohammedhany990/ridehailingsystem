package com.ridehailing.pricingservice.config;

import com.ridehailing.pricingservice.entity.DiscountType;
import com.ridehailing.pricingservice.entity.FareRule;
import com.ridehailing.pricingservice.entity.PromoCode;
import com.ridehailing.pricingservice.repository.FareRuleRepository;
import com.ridehailing.pricingservice.repository.PromoCodeRepository;
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
    private final PromoCodeRepository promoCodeRepository;

    @Override
    public void run(String... args) {
        seedFareRules();
        seedPromoCodes();
    }

    private void seedPromoCodes() {
        seedPromo("RIDE20", DiscountType.PERCENT, "20");
        seedPromo("WELCOME10", DiscountType.FLAT, "10");
    }

    private void seedPromo(String code, DiscountType type, String value) {
        if (promoCodeRepository.findByCodeIgnoreCaseAndActiveTrue(code).isEmpty()) {
            promoCodeRepository.save(PromoCode.builder()
                    .code(code)
                    .discountType(type)
                    .value(new BigDecimal(value))
                    .active(true)
                    .build());
            log.info("Seeded promo code {}", code);
        }
    }

    private void seedFareRules() {
        List<FareRule> defaults = List.of(
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
                    .build(),
                FareRule.builder()
                    .vehicleType("COMFORT")
                    .baseFare(new BigDecimal("15.00"))
                    .ratePerKm(new BigDecimal("7.00"))
                    .ratePerMin(new BigDecimal("1.10"))
                    .minimumFare(new BigDecimal("22.00"))
                    .currency("EGP")
                    .active(true)
                    .build(),
                FareRule.builder()
                    .vehicleType("XL")
                    .baseFare(new BigDecimal("18.00"))
                    .ratePerKm(new BigDecimal("8.00"))
                    .ratePerMin(new BigDecimal("1.25"))
                    .minimumFare(new BigDecimal("26.00"))
                    .currency("EGP")
                    .active(true)
                    .build()
        );
        List<String> newTypes = defaults.stream()
                .map(FareRule::getVehicleType)
                .filter(type -> fareRuleRepository.findByVehicleTypeAndActiveTrue(type).isEmpty())
                .toList();
        if (!newTypes.isEmpty()) {
            log.info("Seeding fare rules for vehicle types: {}", newTypes);
            fareRuleRepository.saveAll(defaults.stream()
                    .filter(rule -> newTypes.contains(rule.getVehicleType()))
                    .toList());
        }
    }
}
