package com.ridehailing.rideservice.client;

import com.ridehailing.rideservice.client.dto.PricingCalculationRequest;
import com.ridehailing.rideservice.client.dto.PricingCalculationResponse;
import com.ridehailing.rideservice.client.dto.UserApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pricing-service", url = "${feign.client.config.pricing-service.url}")
public interface PricingClient {

    @PostMapping("/api/pricing/calculate")
    UserApiResponse<PricingCalculationResponse> calculate(
            @RequestBody PricingCalculationRequest request
    );
}
