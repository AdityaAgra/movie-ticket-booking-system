package com.aditya.movieticketbooking.theater.dto;

import com.aditya.movieticketbooking.common.enums.SeatType;
import com.aditya.movieticketbooking.theater.Seat;
import java.util.UUID;

public record SeatResponse(UUID id, String rowLabel, int seatNumber, SeatType seatType, UUID auditoriumId) {
    public static SeatResponse from(Seat seat) {
        return new SeatResponse(seat.getId(), seat.getRowLabel(), seat.getSeatNumber(), seat.getSeatType(), seat.getAuditorium().getId());
    }
}
