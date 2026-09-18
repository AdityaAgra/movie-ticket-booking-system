package com.aditya.movieticketbooking.booking.dto;

import com.aditya.movieticketbooking.booking.Booking;
import com.aditya.movieticketbooking.common.enums.BookingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BookingHistoryResponse(
        UUID bookingId,
        BookingStatus status,
        BigDecimal totalAmount,
        Instant createdAt,
        UUID showId,
        String movieTitle,
        Instant showStartTime) {
    public static BookingHistoryResponse from(Booking booking) {
        return new BookingHistoryResponse(
                booking.getId(),
                booking.getStatus(),
                booking.getTotalAmount(),
                booking.getCreatedAt(),
                booking.getShow().getId(),
                booking.getShow().getMovie().getTitle(),
                booking.getShow().getStartTime());
    }
}
