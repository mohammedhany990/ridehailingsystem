package com.ridehailing.pricingservice.dto;

import com.ridehailing.pricingservice.entity.DiscountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PromoValidationResponse {

    private String code;
    private boolean valid;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private String message;
}
