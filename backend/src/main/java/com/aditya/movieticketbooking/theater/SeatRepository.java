package com.aditya.movieticketbooking.theater;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, UUID> {
    boolean existsByAuditoriumIdAndRowLabelAndSeatNumber(UUID auditoriumId, String rowLabel, int seatNumber);
}
