package com.aditya.movieticketbooking.booking;

import java.util.UUID;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, UUID> {
    long countByBookingShowId(UUID showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT bookingSeat FROM BookingSeat bookingSeat
            JOIN FETCH bookingSeat.showSeat
            WHERE bookingSeat.booking.id = :bookingId
            """)
    List<BookingSeat> findByBookingIdForUpdate(@Param("bookingId") UUID bookingId);
}
