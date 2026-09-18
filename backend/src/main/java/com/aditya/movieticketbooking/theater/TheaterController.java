package com.aditya.movieticketbooking.theater;

import com.aditya.movieticketbooking.theater.dto.CreateTheaterRequest;
import com.aditya.movieticketbooking.theater.dto.TheaterResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/theaters")
public class TheaterController {
    private final TheaterService theaterService;

    public TheaterController(TheaterService theaterService) { this.theaterService = theaterService; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TheaterResponse create(@Valid @RequestBody CreateTheaterRequest request) {
        return theaterService.create(request);
    }
}
