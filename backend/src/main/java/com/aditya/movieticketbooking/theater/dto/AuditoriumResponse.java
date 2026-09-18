package com.aditya.movieticketbooking.theater.dto;

import com.aditya.movieticketbooking.theater.Auditorium;
import java.util.UUID;

public record AuditoriumResponse(UUID id, String name, UUID theaterId) {
    public static AuditoriumResponse from(Auditorium auditorium) {
        return new AuditoriumResponse(auditorium.getId(), auditorium.getName(), auditorium.getTheater().getId());
    }
}
