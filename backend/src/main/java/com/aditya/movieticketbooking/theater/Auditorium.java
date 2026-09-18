package com.aditya.movieticketbooking.theater;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "auditoriums")
public class Auditorium {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 100)
    private String name;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theater_id", nullable = false)
    private Theater theater;

    protected Auditorium() { }
    private Auditorium(String name, Theater theater) { this.name = name; this.theater = theater; }
    public static Auditorium create(String name, Theater theater) { return new Auditorium(name, theater); }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public Theater getTheater() { return theater; }
}
