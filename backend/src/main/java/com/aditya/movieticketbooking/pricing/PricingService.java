package com.aditya.movieticketbooking.pricing;

import com.aditya.movieticketbooking.common.enums.DiscountType;
import com.aditya.movieticketbooking.common.enums.SeatType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PricingService {
    private final BigDecimal premiumSeatSurcharge;
    private final BigDecimal weekendSurcharge;

    public PricingService(
            @Value("${pricing.premium-seat-surcharge}") BigDecimal premiumSeatSurcharge,
            @Value("${pricing.weekend-surcharge}") BigDecimal weekendSurcharge) {
        this.premiumSeatSurcharge = premiumSeatSurcharge;
        this.weekendSurcharge = weekendSurcharge;
    }

    public PriceQuote calculate(BigDecimal basePrice, SeatType seatType, LocalDate showDate, DiscountCode discountCode) {
        BigDecimal subtotal = basePrice;
        if (seatType == SeatType.PREMIUM) {
            subtotal = subtotal.add(premiumSeatSurcharge);
        }
        if (isWeekend(showDate)) {
            subtotal = subtotal.add(weekendSurcharge);
        }

        BigDecimal discountAmount = calculateDiscount(subtotal, discountCode);
        BigDecimal finalAmount = subtotal.subtract(discountAmount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        return new PriceQuote(subtotal.setScale(2, RoundingMode.HALF_UP), discountAmount, finalAmount);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal, DiscountCode discountCode) {
        if (discountCode == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal discount = discountCode.getDiscountType() == DiscountType.PERCENTAGE
                ? subtotal.multiply(discountCode.getValue()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                : discountCode.getValue();
        return discount.min(subtotal).setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
