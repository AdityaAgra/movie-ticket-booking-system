package com.aditya.movieticketbooking.refund;

import com.aditya.movieticketbooking.common.exception.BusinessConflictException;
import com.aditya.movieticketbooking.common.exception.InvalidRequestException;
import com.aditya.movieticketbooking.refund.dto.CreateRefundPolicyRequest;
import com.aditya.movieticketbooking.refund.dto.RefundPolicyResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefundService {
    private final RefundPolicyRepository refundPolicyRepository;
    public RefundService(RefundPolicyRepository refundPolicyRepository) { this.refundPolicyRepository = refundPolicyRepository; }

    @Transactional
    public RefundPolicyResponse create(CreateRefundPolicyRequest request) {
        if (refundPolicyRepository.existsByMinimumHoursBeforeShow(request.minimumHoursBeforeShow())) {
            throw new BusinessConflictException("A refund policy already exists for this cancellation window.");
        }
        return RefundPolicyResponse.from(refundPolicyRepository.save(RefundPolicy.create(
                request.minimumHoursBeforeShow(), request.refundPercentage(), request.active())));
    }

    @Transactional(readOnly = true)
    public RefundQuote calculateRefund(BigDecimal bookingTotal, Instant showStartTime, Instant cancelledAt) {
        long hoursBeforeShow = Math.max(0, Duration.between(cancelledAt, showStartTime).toHours());
        List<RefundPolicy> policies = refundPolicyRepository.findByActiveTrueOrderByMinimumHoursBeforeShowDesc();
        RefundPolicy policy = policies.stream()
                .filter(candidate -> hoursBeforeShow >= candidate.getMinimumHoursBeforeShow())
                .findFirst()
                .orElseThrow(() -> new InvalidRequestException("No active refund policy applies to this cancellation."));
        BigDecimal amount = bookingTotal.multiply(policy.getRefundPercentage())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return new RefundQuote(policy.getRefundPercentage(), amount);
    }
}
