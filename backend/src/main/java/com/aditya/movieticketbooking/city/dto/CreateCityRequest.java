package com.aditya.movieticketbooking.city.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCityRequest(
        @NotBlank(message = "City name is required.")
        @Size(max = 100, message = "City name must not exceed 100 characters.")
        String name) { }
