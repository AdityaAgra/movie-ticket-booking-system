package com.aditya.movieticketbooking.pricing.dto;

import com.aditya.movieticketbooking.common.enums.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateDiscountCodeRequest(
        @NotBlank(message = "Discount code is required.") @Size(max = 50, message = "Discount code must not exceed 50 characters.") String code,
        @NotNull(message = "Discount type is required.") DiscountType discountType,
        @NotNull(message = "Discount value is required.") @DecimalMin(value = "0.01", message = "Discount value must be positive.") BigDecimal value,
        @NotNull(message = "Discount start time is required.") Instant validFrom,
        @NotNull(message = "Discount end time is required.") Instant validTo,
        boolean active) { }
