package com.aditya.movieticketbooking.booking.dto;

import com.aditya.movieticketbooking.booking.Booking;
import com.aditya.movieticketbooking.common.enums.BookingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record HoldResponse(UUID bookingId, BookingStatus status, Instant holdExpiresAt, BigDecimal totalAmount) {
    public static HoldResponse from(Booking booking, Instant holdExpiresAt) {
        return new HoldResponse(booking.getId(), booking.getStatus(), holdExpiresAt, booking.getTotalAmount());
    }
}
