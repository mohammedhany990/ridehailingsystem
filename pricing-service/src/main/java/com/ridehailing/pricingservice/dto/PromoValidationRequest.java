package com.ridehailing.pricingservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PromoValidationRequest {

    @NotBlank(message = "Promo code is required")
    private String code;
}
