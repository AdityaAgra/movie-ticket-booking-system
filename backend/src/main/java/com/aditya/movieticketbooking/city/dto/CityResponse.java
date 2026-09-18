package com.aditya.movieticketbooking.city.dto;

import com.aditya.movieticketbooking.city.City;
import java.util.UUID;

public record CityResponse(UUID id, String name) {
    public static CityResponse from(City city) { return new CityResponse(city.getId(), city.getName()); }
}
