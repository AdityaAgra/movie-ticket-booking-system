package com.aditya.movieticketbooking.show;

import com.aditya.movieticketbooking.movie.Movie;
import com.aditya.movieticketbooking.theater.Auditorium;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shows")
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auditorium_id", nullable = false)
    private Auditorium auditorium;
    @Column(name = "start_time", nullable = false)
    private Instant startTime;
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    protected Show() { }
    private Show(Movie movie, Auditorium auditorium, Instant startTime, BigDecimal basePrice) {
        this.movie = movie;
        this.auditorium = auditorium;
        this.startTime = startTime;
        this.basePrice = basePrice;
    }
    public static Show create(Movie movie, Auditorium auditorium, Instant startTime, BigDecimal basePrice) {
        return new Show(movie, auditorium, startTime, basePrice);
    }
    public UUID getId() { return id; }
    public Movie getMovie() { return movie; }
    public Auditorium getAuditorium() { return auditorium; }
    public Instant getStartTime() { return startTime; }
    public BigDecimal getBasePrice() { return basePrice; }
}
