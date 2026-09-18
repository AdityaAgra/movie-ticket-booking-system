package com.aditya.movieticketbooking.theater.dto;

import com.aditya.movieticketbooking.common.enums.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateSeatRequest(
        @NotBlank(message = "Seat row is required.")
        @Size(max = 10, message = "Seat row must not exceed 10 characters.")
        String rowLabel,
        @Positive(message = "Seat number must be greater than zero.")
        int seatNumber,
        @NotNull(message = "Seat type is required.")
        SeatType seatType) { }
