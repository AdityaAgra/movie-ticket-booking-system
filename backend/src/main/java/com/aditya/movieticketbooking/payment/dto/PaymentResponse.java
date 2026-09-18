package com.aditya.movieticketbooking.payment.dto;

import com.aditya.movieticketbooking.common.enums.BookingStatus;
import com.aditya.movieticketbooking.common.enums.PaymentStatus;
import com.aditya.movieticketbooking.payment.Payment;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId, UUID bookingId, PaymentStatus paymentStatus, BookingStatus bookingStatus, BigDecimal amount) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getStatus(),
                payment.getBooking().getStatus(),
                payment.getAmount());
    }
}
