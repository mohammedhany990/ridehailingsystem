package com.ridehailing.pricingservice.service;

import com.ridehailing.pricingservice.dto.*;
import com.ridehailing.pricingservice.entity.FareCalculation;
import com.ridehailing.pricingservice.entity.FareRule;
import com.ridehailing.pricingservice.repository.FareCalculationRepository;
import com.ridehailing.pricingservice.repository.FareRuleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PricingService {

    private final FareRuleRepository fareRuleRepository;
    private final FareCalculationRepository fareCalculationRepository;
    private final SurgeService surgeService;


    public FareEstimateResponse estimate(FareEstimateRequest request) {

        FareRule rule = getRule(request.getVehicleType());

        // Use provided distance/duration or fallback to a rough estimate
        double distKm  = request.getDistanceKm()  != null ? request.getDistanceKm()  : 5.0;
        double durMin  = request.getDurationMin()  != null ? request.getDurationMin() : 15.0;

        BigDecimal surge = surgeService.getSurgeMultiplier(
                request.getPickupLat(), request.getPickupLng());

        BigDecimal base   = rule.getBaseFare();
        BigDecimal distBd = BigDecimal.valueOf(distKm).setScale(3, RoundingMode.HALF_UP);
        BigDecimal durBd  = BigDecimal.valueOf(durMin).setScale(2, RoundingMode.HALF_UP);

        BigDecimal distCost = rule.getRatePerKm().multiply(distBd);
        BigDecimal timeCost = rule.getRatePerMin().multiply(durBd);
        BigDecimal rawTotal = base.add(distCost).add(timeCost).multiply(surge);
        BigDecimal total    = rawTotal.max(rule.getMinimumFare()).setScale(2, RoundingMode.HALF_UP);

        // ±15 % buffer for estimate range
        BigDecimal factor   = new BigDecimal("0.15");
        BigDecimal minFare  = total.multiply(BigDecimal.ONE.subtract(factor)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal maxFare  = total.multiply(BigDecimal.ONE.add(factor)).setScale(2, RoundingMode.HALF_UP);

        return FareEstimateResponse.builder()
                .estimatedMinFare(minFare)
                .estimatedMaxFare(maxFare)
                .surgeMultiplier(surge)
                .currency(rule.getCurrency())
                .vehicleType(request.getVehicleType())
                .distanceKm(distKm)
                .durationMinutes(durMin)
                .build();
    }

    public FareCalculationResponse calculate(FareCalculationRequest request) {

        // Idempotency: return existing calculation if already done
        return fareCalculationRepository.findByRideId(request.getRideId())
                .map(this::toResponse)
                .orElseGet(() -> doCalculate(request));
    }

    private FareCalculationResponse doCalculate(FareCalculationRequest request) {

        FareRule rule = getRule(request.getVehicleType());

        BigDecimal surge    = surgeService.getSurgeMultiplier(0, 0); // no coords on completion path
        BigDecimal distBd   = BigDecimal.valueOf(request.getDistanceKm()).setScale(3, RoundingMode.HALF_UP);
        BigDecimal durBd    = BigDecimal.valueOf(request.getDurationMinutes()).setScale(2, RoundingMode.HALF_UP);

        BigDecimal distCost = rule.getRatePerKm().multiply(distBd);
        BigDecimal timeCost = rule.getRatePerMin().multiply(durBd);
        BigDecimal rawTotal = rule.getBaseFare().add(distCost).add(timeCost).multiply(surge);
        BigDecimal total    = rawTotal.max(rule.getMinimumFare()).setScale(2, RoundingMode.HALF_UP);

        FareCalculation calc = FareCalculation.builder()
                .rideId(request.getRideId())
                .vehicleType(request.getVehicleType())
                .baseFare(rule.getBaseFare())
                .distanceKm(distBd)
                .durationMinutes(durBd)
                .surgeMultiplier(surge)
                .distanceCost(distCost.setScale(2, RoundingMode.HALF_UP))
                .timeCost(timeCost.setScale(2, RoundingMode.HALF_UP))
                .totalAmount(total)
                .currency(rule.getCurrency())
                .build();

        FareCalculation saved = fareCalculationRepository.save(calc);
        log.info("Fare calculated: rideId={}, total={} {}", request.getRideId(), total, rule.getCurrency());
        return toResponse(saved);
    }

    public ApiResponse<FareCalculationResponse> getByRideId(UUID rideId) {
        return fareCalculationRepository.findByRideId(rideId)
                .map(fc -> ApiResponse.success("Fare found", toResponse(fc)))
                .orElse(ApiResponse.error("No fare calculated for ride: " + rideId));
    }

    private FareRule getRule(String vehicleType) {
        return fareRuleRepository.findByVehicleTypeAndActiveTrue(vehicleType)
                .orElseGet(() -> fareRuleRepository.findByVehicleTypeAndActiveTrue("STANDARD")
                        .orElseThrow(() -> new IllegalStateException(
                                "No active fare rule found for vehicle type: " + vehicleType)));
    }

    private FareCalculationResponse toResponse(FareCalculation fc) {
        return FareCalculationResponse.builder()
                .id(fc.getId())
                .rideId(fc.getRideId())
                .vehicleType(fc.getVehicleType())
                .baseFare(fc.getBaseFare())
                .distanceKm(fc.getDistanceKm())
                .durationMinutes(fc.getDurationMinutes())
                .surgeMultiplier(fc.getSurgeMultiplier())
                .distanceCost(fc.getDistanceCost())
                .timeCost(fc.getTimeCost())
                .totalAmount(fc.getTotalAmount())
                .currency(fc.getCurrency())
                .calculatedAt(fc.getCalculatedAt())
                .build();
    }
}
