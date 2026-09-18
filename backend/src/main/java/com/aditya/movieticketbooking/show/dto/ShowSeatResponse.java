package com.aditya.movieticketbooking.show.dto;

import com.aditya.movieticketbooking.common.enums.SeatType;
import com.aditya.movieticketbooking.common.enums.ShowSeatStatus;
import com.aditya.movieticketbooking.show.ShowSeat;
import java.util.UUID;

public record ShowSeatResponse(
        UUID id,
        UUID seatId,
        String rowLabel,
        int seatNumber,
        SeatType seatType,
        ShowSeatStatus status) {
    public static ShowSeatResponse from(ShowSeat showSeat) {
        return new ShowSeatResponse(
                showSeat.getId(),
                showSeat.getSeat().getId(),
                showSeat.getSeat().getRowLabel(),
                showSeat.getSeat().getSeatNumber(),
                showSeat.getSeat().getSeatType(),
                showSeat.getStatus());
    }
}
