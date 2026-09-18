package com.aditya.movieticketbooking.theater;

import com.aditya.movieticketbooking.common.exception.BusinessConflictException;
import com.aditya.movieticketbooking.common.exception.ResourceNotFoundException;
import com.aditya.movieticketbooking.theater.dto.CreateSeatRequest;
import com.aditya.movieticketbooking.theater.dto.SeatResponse;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeatService {
    private final SeatRepository seatRepository;
    private final AuditoriumRepository auditoriumRepository;

    public SeatService(SeatRepository seatRepository, AuditoriumRepository auditoriumRepository) {
        this.seatRepository = seatRepository;
        this.auditoriumRepository = auditoriumRepository;
    }

    @Transactional
    public SeatResponse create(UUID auditoriumId, CreateSeatRequest request) {
        Auditorium auditorium = auditoriumRepository.findById(auditoriumId)
                .orElseThrow(() -> new ResourceNotFoundException("Auditorium not found."));
        String rowLabel = request.rowLabel().trim();
        if (seatRepository.existsByAuditoriumIdAndRowLabelAndSeatNumber(auditoriumId, rowLabel, request.seatNumber())) {
            throw new BusinessConflictException("A seat already exists at this position.");
        }
        return SeatResponse.from(seatRepository.save(
                Seat.create(rowLabel, request.seatNumber(), request.seatType(), auditorium)));
    }
}
