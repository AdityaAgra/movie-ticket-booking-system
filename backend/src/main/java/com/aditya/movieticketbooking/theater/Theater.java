package com.aditya.movieticketbooking.theater;

import com.aditya.movieticketbooking.city.City;
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
@Table(name = "theaters")
public class Theater {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    protected Theater() { }

    private Theater(String name, String address, City city) {
        this.name = name;
        this.address = address;
        this.city = city;
    }

    public static Theater create(String name, String address, City city) {
        return new Theater(name, address, city);
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public City getCity() { return city; }
}
