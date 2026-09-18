package com.aditya.movieticketbooking.theater.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateTheaterRequest(
        @NotBlank(message = "Theater name is required.")
        @Size(max = 150, message = "Theater name must not exceed 150 characters.")
        String name,
        @NotBlank(message = "Theater address is required.")
        @Size(max = 255, message = "Theater address must not exceed 255 characters.")
        String address,
        @NotNull(message = "City ID is required.")
        UUID cityId) { }
