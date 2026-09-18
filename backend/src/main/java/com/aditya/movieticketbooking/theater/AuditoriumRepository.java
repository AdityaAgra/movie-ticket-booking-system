package com.aditya.movieticketbooking.theater;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriumRepository extends JpaRepository<Auditorium, UUID> { }
