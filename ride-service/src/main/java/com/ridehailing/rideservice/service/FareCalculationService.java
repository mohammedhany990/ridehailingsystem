package com.ridehailing.rideservice.service;

import com.ridehailing.rideservice.client.PricingClient;
import com.ridehailing.rideservice.client.dto.PricingCalculationRequest;
import com.ridehailing.rideservice.client.dto.PricingCalculationResponse;
import com.ridehailing.rideservice.client.dto.UserApiResponse;
import com.ridehailing.rideservice.entity.Fare;
import com.ridehailing.rideservice.entity.Ride;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class FareCalculationService {

    private final PricingClient pricingClient;

    @CircuitBreaker(name = "pricing", fallbackMethod = "pricingFallback")
    @Retry(name = "pricing")
    public Fare calculate(Ride ride, double distanceKm, double durationMinutes) {

        PricingCalculationRequest request = PricingCalculationRequest.builder()
                .rideId(ride.getId())
                .distanceKm(distanceKm)
                .durationMinutes(durationMinutes)
                .vehicleType("STANDARD")
                .build();

        UserApiResponse<PricingCalculationResponse> response =
                pricingClient.calculate(request);

        if (response == null || !response.isSuccess() || response.getData() == null) {
            throw new RuntimeException("Invalid pricing response");
        }

        PricingCalculationResponse data = response.getData();

        return Fare.builder()
                .ride(ride)
                .baseFare(data.getBaseFare())
                .distanceKm(data.getDistanceKm())
                .surgeMultiplier(data.getSurgeMultiplier())
                .totalAmount(data.getTotalAmount())
                .currency(data.getCurrency() != null ? data.getCurrency() : "EGP")
                .calculatedAt(data.getCalculatedAt() != null
                        ? data.getCalculatedAt()
                        : Instant.now())
                .build();
    }


    //Called after all retries fail or the circuit breaker is OPEN.
    public Fare pricingFallback(
            Ride ride,
            double distanceKm,
            double durationMinutes,
            Exception ex
    ) {

        log.error("Pricing service unavailable. Using fallback fare. Cause: {}",
                ex.getMessage());

        BigDecimal distance = BigDecimal.valueOf(distanceKm);

        BigDecimal base = new BigDecimal("10.00");
        BigDecimal ratePerKm = new BigDecimal("5.00");

        BigDecimal total = base.add(ratePerKm.multiply(distance));

        return Fare.builder()
                .ride(ride)
                .baseFare(base)
                .distanceKm(distance)
                .surgeMultiplier(BigDecimal.ONE)
                .totalAmount(total)
                .currency("EGP")
                .calculatedAt(Instant.now())
                .build();
    }
}