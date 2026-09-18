package com.aditya.movieticketbooking.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.aditya.movieticketbooking.common.enums.DiscountType;
import com.aditya.movieticketbooking.common.enums.SeatType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PricingServiceTest {
    private final PricingService pricingService = new PricingService(new BigDecimal("50.00"), new BigDecimal("25.00"));

    @Test
    void regularWeekdayUsesBasePrice() {
        PriceQuote quote = pricingService.calculate(new BigDecimal("200.00"), SeatType.REGULAR, LocalDate.of(2026, 9, 21), null);
        assertEquals(new BigDecimal("200.00"), quote.finalAmount());
    }

    @Test
    void premiumWeekendAddsBothSurcharges() {
        PriceQuote quote = pricingService.calculate(new BigDecimal("200.00"), SeatType.PREMIUM, LocalDate.of(2026, 9, 19), null);
        assertEquals(new BigDecimal("275.00"), quote.subtotal());
        assertEquals(new BigDecimal("275.00"), quote.finalAmount());
    }

    @Test
    void percentageDiscountIsAppliedToCalculatedSubtotal() {
        DiscountCode discount = DiscountCode.create(
                "TEN", DiscountType.PERCENTAGE, new BigDecimal("10"), Instant.now().minusSeconds(60), Instant.now().plusSeconds(60), true);
        PriceQuote quote = pricingService.calculate(new BigDecimal("200.00"), SeatType.REGULAR, LocalDate.of(2026, 9, 21), discount);
        assertEquals(new BigDecimal("20.00"), quote.discountAmount());
        assertEquals(new BigDecimal("180.00"), quote.finalAmount());
    }
}
