package com.ridehailing.pricingservice.service;

import com.ridehailing.pricingservice.dto.FareEstimateRequest;
import com.ridehailing.pricingservice.dto.FareEstimateResponse;
import com.ridehailing.pricingservice.entity.DiscountType;
import com.ridehailing.pricingservice.entity.FareRule;
import com.ridehailing.pricingservice.entity.PromoCode;
import com.ridehailing.pricingservice.repository.FareCalculationRepository;
import com.ridehailing.pricingservice.repository.FareRuleRepository;
import com.ridehailing.pricingservice.repository.PromoCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private FareRuleRepository fareRuleRepository;

    @Mock
    private FareCalculationRepository fareCalculationRepository;

    @Mock
    private SurgeService surgeService;

    @Mock
    private PromoCodeRepository promoCodeRepository;

    private PricingService pricingService;

    private FareRule standardRule;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService(fareRuleRepository, fareCalculationRepository, surgeService, promoCodeRepository);
        standardRule = FareRule.builder()
                .vehicleType("STANDARD")
                .baseFare(new BigDecimal("10.00"))
                .ratePerKm(new BigDecimal("5.00"))
                .ratePerMin(new BigDecimal("1.00"))
                .minimumFare(new BigDecimal("15.00"))
                .currency("EGP")
                .build();
    }

    private FareEstimateRequest request(Double km, Double min) {
        FareEstimateRequest r = new FareEstimateRequest();
        r.setVehicleType("STANDARD");
        r.setDistanceKm(km);
        r.setDurationMin(min);
        r.setPickupLat(30.0);
        r.setPickupLng(31.0);
        return r;
    }

    @Test
    void estimateIsBasePlusDistancePlusTimeWithSurge() {
        when(fareRuleRepository.findByVehicleTypeAndActiveTrue("STANDARD")).thenReturn(Optional.of(standardRule));
        when(surgeService.getSurgeMultiplier(anyDouble(), anyDouble())).thenReturn(new BigDecimal("1.0"));

        FareEstimateResponse r = pricingService.estimate(request(10.0, 20.0));

        BigDecimal expected = new BigDecimal("80.00");
        assertThat(r.getEstimatedMinFare()).isEqualByComparingTo(expected.multiply(new BigDecimal("0.85")));
        assertThat(r.getEstimatedMaxFare()).isEqualByComparingTo(expected.multiply(new BigDecimal("1.15")));
        assertThat(r.getCurrency()).isEqualTo("EGP");
    }

    @Test
    void surgeMultiplierRaisesTheFare() {
        when(fareRuleRepository.findByVehicleTypeAndActiveTrue("STANDARD")).thenReturn(Optional.of(standardRule));
        when(surgeService.getSurgeMultiplier(anyDouble(), anyDouble())).thenReturn(new BigDecimal("2.0"));

        FareEstimateResponse r = pricingService.estimate(request(10.0, 20.0));

        assertThat(r.getEstimatedMaxFare()).isGreaterThan(new BigDecimal("160.00"));
        assertThat(r.getSurgeMultiplier()).isEqualByComparingTo("2.0");
    }

    @Test
    void minimumFareFloorsCheapTrips() {
        when(fareRuleRepository.findByVehicleTypeAndActiveTrue("STANDARD")).thenReturn(Optional.of(standardRule));
        when(surgeService.getSurgeMultiplier(anyDouble(), anyDouble())).thenReturn(BigDecimal.ONE);

        FareEstimateResponse r = pricingService.estimate(request(0.1, 0.5));

        assertThat(r.getEstimatedMinFare()).isGreaterThanOrEqualTo(new BigDecimal("12.75"));
    }

    @Test
    void percentPromoDiscountsTheEstimate() {
        when(fareRuleRepository.findByVehicleTypeAndActiveTrue("STANDARD")).thenReturn(Optional.of(standardRule));
        when(surgeService.getSurgeMultiplier(anyDouble(), anyDouble())).thenReturn(BigDecimal.ONE);
        when(promoCodeRepository.findByCodeIgnoreCaseAndActiveTrue("RIDE20"))
                .thenReturn(Optional.of(PromoCode.builder()
                        .code("RIDE20")
                        .discountType(DiscountType.PERCENT)
                        .value(new BigDecimal("20"))
                        .active(true)
                        .build()));

        FareEstimateRequest r = request(10.0, 20.0);
        r.setPromoCode("RIDE20");

        FareEstimateResponse res = pricingService.estimate(r);

        assertThat(res.getDiscountAmount()).isEqualByComparingTo("16.00");
        assertThat(res.getPromoCode()).isEqualTo("RIDE20");
        assertThat(res.getEstimatedMaxFare()).isEqualByComparingTo("73.60");
    }

    @Test
    void missingDistanceFallsBackToDefaults() {
        when(fareRuleRepository.findByVehicleTypeAndActiveTrue("STANDARD")).thenReturn(Optional.of(standardRule));
        when(surgeService.getSurgeMultiplier(anyDouble(), anyDouble())).thenReturn(BigDecimal.ONE);

        FareEstimateResponse r = pricingService.estimate(request(null, null));

        assertThat(r.getDistanceKm()).isEqualTo(5.0);
        assertThat(r.getDurationMinutes()).isEqualTo(15.0);
    }
}
