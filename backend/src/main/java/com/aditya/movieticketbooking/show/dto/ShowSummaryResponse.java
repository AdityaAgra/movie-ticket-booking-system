package com.aditya.movieticketbooking.show.dto;

import com.aditya.movieticketbooking.show.Show;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ShowSummaryResponse(
        UUID id,
        UUID movieId,
        String movieTitle,
        UUID auditoriumId,
        String auditoriumName,
        Instant startTime,
        BigDecimal basePrice) {
    public static ShowSummaryResponse from(Show show) {
        return new ShowSummaryResponse(
                show.getId(),
                show.getMovie().getId(),
                show.getMovie().getTitle(),
                show.getAuditorium().getId(),
                show.getAuditorium().getName(),
                show.getStartTime(),
                show.getBasePrice());
    }
}
