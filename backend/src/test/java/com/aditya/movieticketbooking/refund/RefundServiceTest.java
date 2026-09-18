package com.aditya.movieticketbooking.refund;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {
    @Mock private RefundPolicyRepository refundPolicyRepository;

    @Test
    void selectsBestMatchingRefundWindow() {
        when(refundPolicyRepository.findByActiveTrueOrderByMinimumHoursBeforeShowDesc()).thenReturn(List.of(
                RefundPolicy.create(24, new BigDecimal("100.00"), true),
                RefundPolicy.create(2, new BigDecimal("50.00"), true),
                RefundPolicy.create(0, new BigDecimal("0.00"), true)));
        RefundService refundService = new RefundService(refundPolicyRepository);
        Instant cancelledAt = Instant.now();

        RefundQuote fullRefund = refundService.calculateRefund(new BigDecimal("200.00"), cancelledAt.plus(30, ChronoUnit.HOURS), cancelledAt);
        RefundQuote partialRefund = refundService.calculateRefund(new BigDecimal("200.00"), cancelledAt.plus(5, ChronoUnit.HOURS), cancelledAt);
        RefundQuote noRefund = refundService.calculateRefund(new BigDecimal("200.00"), cancelledAt.plus(1, ChronoUnit.HOURS), cancelledAt);

        assertEquals(new BigDecimal("200.00"), fullRefund.refundAmount());
        assertEquals(new BigDecimal("100.00"), partialRefund.refundAmount());
        assertEquals(new BigDecimal("0.00"), noRefund.refundAmount());
    }
}
