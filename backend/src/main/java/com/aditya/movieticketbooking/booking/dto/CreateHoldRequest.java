package com.aditya.movieticketbooking.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CreateHoldRequest(
        @NotEmpty(message = "At least one seat must be selected.") List<@NotNull UUID> seatIds,
        @Size(max = 50, message = "Discount code must not exceed 50 characters.") String discountCode) { }
