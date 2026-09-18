package com.aditya.movieticketbooking.city;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "cities")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    protected City() { }

    private City(String name) { this.name = name; }

    public static City create(String name) { return new City(name); }
    public UUID getId() { return id; }
    public String getName() { return name; }
}
