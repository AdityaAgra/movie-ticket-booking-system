package com.aditya.movieticketbooking.city;

import com.aditya.movieticketbooking.city.dto.CityResponse;
import com.aditya.movieticketbooking.city.dto.CreateCityRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cities")
public class CityController {
    private final CityService cityService;

    public CityController(CityService cityService) { this.cityService = cityService; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CityResponse create(@Valid @RequestBody CreateCityRequest request) {
        return cityService.create(request);
    }

    @GetMapping
    public List<CityResponse> findAll() {
        return cityService.findAll();
    }
}
