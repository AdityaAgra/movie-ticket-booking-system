package com.aditya.movieticketbooking.movie;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    protected Movie() { }
    private Movie(String title, int durationMinutes) { this.title = title; this.durationMinutes = durationMinutes; }
    public static Movie create(String title, int durationMinutes) { return new Movie(title, durationMinutes); }
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public int getDurationMinutes() { return durationMinutes; }
}
