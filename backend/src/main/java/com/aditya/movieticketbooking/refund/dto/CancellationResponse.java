package com.aditya.movieticketbooking.refund.dto;

import com.aditya.movieticketbooking.common.enums.BookingStatus;
import com.aditya.movieticketbooking.common.enums.PaymentStatus;
import com.aditya.movieticketbooking.payment.Payment;
import com.aditya.movieticketbooking.refund.RefundQuote;
import java.math.BigDecimal;
import java.util.UUID;

public record CancellationResponse(
        UUID bookingId,
        BookingStatus bookingStatus,
        UUID refundPaymentId,
        PaymentStatus refundPaymentStatus,
        BigDecimal refundAmount,
        BigDecimal refundPercentage,
        boolean seatsReleased) {
    public static CancellationResponse from(Payment refundPayment, RefundQuote quote) {
        return new CancellationResponse(
                refundPayment.getBooking().getId(),
                refundPayment.getBooking().getStatus(),
                refundPayment.getId(),
                refundPayment.getStatus(),
                quote.refundAmount(),
                quote.refundPercentage(),
                true);
    }
}
