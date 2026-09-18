package com.aditya.movieticketbooking.movie;

import com.aditya.movieticketbooking.movie.dto.CreateMovieRequest;
import com.aditya.movieticketbooking.movie.dto.MovieResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {
    private final MovieService movieService;
    public MovieController(MovieService movieService) { this.movieService = movieService; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovieResponse create(@Valid @RequestBody CreateMovieRequest request) {
        return movieService.create(request);
    }
}
