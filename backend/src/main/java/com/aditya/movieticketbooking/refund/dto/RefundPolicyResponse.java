package com.aditya.movieticketbooking.refund.dto;

import com.aditya.movieticketbooking.refund.RefundPolicy;
import java.math.BigDecimal;
import java.util.UUID;

public record RefundPolicyResponse(UUID id, int minimumHoursBeforeShow, BigDecimal refundPercentage, boolean active) {
    public static RefundPolicyResponse from(RefundPolicy policy) {
        return new RefundPolicyResponse(policy.getId(), policy.getMinimumHoursBeforeShow(), policy.getRefundPercentage(), policy.isActive());
    }
}
