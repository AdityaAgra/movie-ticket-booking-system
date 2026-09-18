package com.aditya.movieticketbooking.show;

import java.util.List;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, UUID> {
    long countByShowId(UUID showId);
    List<ShowSeat> findByShowId(UUID showId);

    @Query("""
            SELECT showSeat FROM ShowSeat showSeat
            JOIN FETCH showSeat.seat
            WHERE showSeat.show.id = :showId
            ORDER BY showSeat.seat.rowLabel ASC, showSeat.seat.seatNumber ASC
            """)
    List<ShowSeat> findSeatMapByShowId(@Param("showId") UUID showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT showSeat FROM ShowSeat showSeat
            JOIN FETCH showSeat.seat
            WHERE showSeat.show.id = :showId
              AND showSeat.seat.id IN :seatIds
            """)
    List<ShowSeat> findByShowIdAndSeatIdsForUpdate(
            @Param("showId") UUID showId, @Param("seatIds") List<UUID> seatIds);
}
