package com.aditya.movieticketbooking.movie.dto;

import com.aditya.movieticketbooking.movie.Movie;
import java.util.UUID;

public record MovieResponse(UUID id, String title, int durationMinutes) {
    public static MovieResponse from(Movie movie) {
        return new MovieResponse(movie.getId(), movie.getTitle(), movie.getDurationMinutes());
    }
}
