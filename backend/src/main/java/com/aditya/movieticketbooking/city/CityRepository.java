package com.aditya.movieticketbooking.city;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, UUID> {
    boolean existsByNameIgnoreCase(String name);

    List<City> findAllByOrderByNameAsc();
}
