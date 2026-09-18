package com.aditya.movieticketbooking.theater;

import com.aditya.movieticketbooking.city.City;
import com.aditya.movieticketbooking.city.CityRepository;
import com.aditya.movieticketbooking.common.exception.ResourceNotFoundException;
import com.aditya.movieticketbooking.theater.dto.CreateTheaterRequest;
import com.aditya.movieticketbooking.theater.dto.TheaterResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TheaterService {
    private final TheaterRepository theaterRepository;
    private final CityRepository cityRepository;

    public TheaterService(TheaterRepository theaterRepository, CityRepository cityRepository) {
        this.theaterRepository = theaterRepository;
        this.cityRepository = cityRepository;
    }

    @Transactional
    public TheaterResponse create(CreateTheaterRequest request) {
        City city = cityRepository.findById(request.cityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found."));
        Theater theater = Theater.create(request.name().trim(), request.address().trim(), city);
        return TheaterResponse.from(theaterRepository.save(theater));
    }
}
