package com.ridehailing.rideservice.service;

import com.ridehailing.rideservice.client.PricingClient;
import com.ridehailing.rideservice.client.dto.PricingCalculationRequest;
import com.ridehailing.rideservice.client.dto.PricingCalculationResponse;
import com.ridehailing.rideservice.client.dto.UserApiResponse;
import com.ridehailing.rideservice.entity.Fare;
import com.ridehailing.rideservice.entity.Ride;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class FareCalculationService {

    private final PricingClient pricingClient;

    public Fare calculate(Ride ride, double distanceKm, double durationMinutes) {

        PricingCalculationRequest request = PricingCalculationRequest.builder()
                .rideId(ride.getId())
                .distanceKm(distanceKm)
                .durationMinutes(durationMinutes)
                .vehicleType("STANDARD")
                .build();

        try {
            UserApiResponse<PricingCalculationResponse> response = pricingClient.calculate(request);

            if (response != null && response.isSuccess() && response.getData() != null) {

                PricingCalculationResponse data = response.getData();

                return Fare.builder()
                        .ride(ride)
                        .baseFare(data.getBaseFare())
                        .distanceKm(data.getDistanceKm())
                        .surgeMultiplier(data.getSurgeMultiplier())
                        .totalAmount(data.getTotalAmount())
                        .currency(data.getCurrency() != null ? data.getCurrency() : "EGP")
                        .calculatedAt(data.getCalculatedAt() != null ? data.getCalculatedAt() : Instant.now())
                        .build();
            }

            throw new RuntimeException("Invalid pricing response");

        } catch (Exception e) {

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
}