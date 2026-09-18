package com.aditya.movieticketbooking.movie;

import com.aditya.movieticketbooking.movie.dto.CreateMovieRequest;
import com.aditya.movieticketbooking.movie.dto.MovieResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MovieService {
    private final MovieRepository movieRepository;
    public MovieService(MovieRepository movieRepository) { this.movieRepository = movieRepository; }

    @Transactional
    public MovieResponse create(CreateMovieRequest request) {
        return MovieResponse.from(movieRepository.save(Movie.create(request.title().trim(), request.durationMinutes())));
    }
}
