package com.aditya.movieticketbooking.theater;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TheaterRepository extends JpaRepository<Theater, UUID> { }
