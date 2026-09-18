package com.aditya.movieticketbooking.booking;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    long countByShowId(UUID showId);

    @Query("""
            SELECT DISTINCT booking FROM Booking booking
            JOIN FETCH booking.show show
            JOIN FETCH show.movie
            WHERE booking.user.email = :email
            ORDER BY booking.createdAt DESC
            """)
    List<Booking> findHistoryByUserEmailOrderByCreatedAtDesc(@Param("email") String email);

    @Query("""
            SELECT DISTINCT booking FROM Booking booking
            JOIN BookingSeat bookingSeat ON bookingSeat.booking = booking
            WHERE booking.status = com.aditya.movieticketbooking.common.enums.BookingStatus.PENDING
              AND bookingSeat.showSeat.id IN :showSeatIds
            """)
    List<Booking> findPendingByShowSeatIds(@Param("showSeatIds") List<UUID> showSeatIds);
}
