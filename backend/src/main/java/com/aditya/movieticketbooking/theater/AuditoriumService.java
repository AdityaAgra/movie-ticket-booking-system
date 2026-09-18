package com.aditya.movieticketbooking.theater;

import com.aditya.movieticketbooking.common.exception.ResourceNotFoundException;
import com.aditya.movieticketbooking.theater.dto.AuditoriumResponse;
import com.aditya.movieticketbooking.theater.dto.CreateAuditoriumRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriumService {
    private final AuditoriumRepository auditoriumRepository;
    private final TheaterRepository theaterRepository;

    public AuditoriumService(AuditoriumRepository auditoriumRepository, TheaterRepository theaterRepository) {
        this.auditoriumRepository = auditoriumRepository;
        this.theaterRepository = theaterRepository;
    }

    @Transactional
    public AuditoriumResponse create(CreateAuditoriumRequest request) {
        Theater theater = theaterRepository.findById(request.theaterId())
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found."));
        return AuditoriumResponse.from(auditoriumRepository.save(Auditorium.create(request.name().trim(), theater)));
    }
}
