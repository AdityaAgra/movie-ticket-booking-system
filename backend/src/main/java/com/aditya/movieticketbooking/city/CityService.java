package com.aditya.movieticketbooking.city;

import com.aditya.movieticketbooking.city.dto.CityResponse;
import com.aditya.movieticketbooking.city.dto.CreateCityRequest;
import com.aditya.movieticketbooking.common.exception.BusinessConflictException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CityService {
    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) { this.cityRepository = cityRepository; }

    @Transactional
    public CityResponse create(CreateCityRequest request) {
        String name = request.name().trim();
        if (cityRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessConflictException("A city with this name already exists.");
        }
        return CityResponse.from(cityRepository.save(City.create(name)));
    }

    @Transactional(readOnly = true)
    public List<CityResponse> findAll() {
        return cityRepository.findAllByOrderByNameAsc().stream().map(CityResponse::from).toList();
    }
}
