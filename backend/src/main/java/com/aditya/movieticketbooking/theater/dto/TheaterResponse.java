package com.aditya.movieticketbooking.theater.dto;

import com.aditya.movieticketbooking.theater.Theater;
import java.util.UUID;

public record TheaterResponse(UUID id, String name, String address, UUID cityId) {
    public static TheaterResponse from(Theater theater) {
        return new TheaterResponse(theater.getId(), theater.getName(), theater.getAddress(), theater.getCity().getId());
    }
}
