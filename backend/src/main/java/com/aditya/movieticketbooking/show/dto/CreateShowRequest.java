package com.aditya.movieticketbooking.show.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateShowRequest(
        @NotNull(message = "Movie ID is required.") UUID movieId,
        @NotNull(message = "Auditorium ID is required.") UUID auditoriumId,
        @NotNull(message = "Show start time is required.") @Future(message = "Show start time must be in the future.") Instant startTime,
        @NotNull(message = "Base price is required.") @PositiveOrZero(message = "Base price cannot be negative.") BigDecimal basePrice) { }
