package com.aditya.movieticketbooking.movie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateMovieRequest(
        @NotBlank(message = "Movie title is required.")
        @Size(max = 255, message = "Movie title must not exceed 255 characters.")
        String title,
        @Positive(message = "Movie duration must be greater than zero.")
        int durationMinutes) { }
