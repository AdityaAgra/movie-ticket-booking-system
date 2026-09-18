package com.aditya.movieticketbooking.booking;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, UUID> {
    long countByBookingShowId(UUID showId);
}
