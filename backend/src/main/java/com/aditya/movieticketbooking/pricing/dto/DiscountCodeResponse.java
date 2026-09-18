package com.aditya.movieticketbooking.pricing.dto;

import com.aditya.movieticketbooking.common.enums.DiscountType;
import com.aditya.movieticketbooking.pricing.DiscountCode;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DiscountCodeResponse(UUID id, String code, DiscountType discountType, BigDecimal value, Instant validFrom, Instant validTo, boolean active) {
    public static DiscountCodeResponse from(DiscountCode code) {
        return new DiscountCodeResponse(code.getId(), code.getCode(), code.getDiscountType(), code.getValue(), code.getValidFrom(), code.getValidTo(), code.isActive());
    }
}
