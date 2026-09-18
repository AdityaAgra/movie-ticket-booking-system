package com.aditya.movieticketbooking.theater.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateAuditoriumRequest(
        @NotBlank(message = "Auditorium name is required.")
        @Size(max = 100, message = "Auditorium name must not exceed 100 characters.")
        String name,
        @NotNull(message = "Theater ID is required.")
        UUID theaterId) { }
