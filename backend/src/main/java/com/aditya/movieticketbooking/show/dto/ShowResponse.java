package com.aditya.movieticketbooking.show.dto;

import com.aditya.movieticketbooking.show.Show;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ShowResponse(
        UUID id,
        UUID movieId,
        UUID auditoriumId,
        Instant startTime,
        BigDecimal basePrice,
        int showSeatCount) {
    public static ShowResponse from(Show show, int showSeatCount) {
        return new ShowResponse(
                show.getId(),
                show.getMovie().getId(),
                show.getAuditorium().getId(),
                show.getStartTime(),
                show.getBasePrice(),
                showSeatCount);
    }
}
