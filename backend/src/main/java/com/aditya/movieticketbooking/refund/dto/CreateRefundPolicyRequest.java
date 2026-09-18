package com.aditya.movieticketbooking.refund.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CreateRefundPolicyRequest(
        @PositiveOrZero(message = "Minimum hours before show cannot be negative.") int minimumHoursBeforeShow,
        @NotNull(message = "Refund percentage is required.")
        @DecimalMin(value = "0.00", message = "Refund percentage cannot be negative.")
        @DecimalMax(value = "100.00", message = "Refund percentage cannot exceed 100.")
        BigDecimal refundPercentage,
        boolean active) { }
