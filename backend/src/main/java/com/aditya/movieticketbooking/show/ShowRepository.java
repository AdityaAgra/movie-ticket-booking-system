package com.aditya.movieticketbooking.show;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShowRepository extends JpaRepository<Show, UUID> {

    @Query("""
            SELECT s FROM Show s
            JOIN FETCH s.movie
            JOIN FETCH s.auditorium auditorium
            JOIN FETCH auditorium.theater theater
            JOIN FETCH theater.city
            WHERE theater.city.id = :cityId
              AND s.startTime >= :startTime
              AND s.startTime < :endTime
            ORDER BY s.startTime ASC
            """)
    List<Show> findByCityAndStartTimeRange(
            @Param("cityId") UUID cityId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);
}
