package com.aditya.movieticketbooking.booking;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    long countByShowId(UUID showId);
}
