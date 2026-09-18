package com.aditya.movieticketbooking.theater;

import com.aditya.movieticketbooking.theater.dto.AuditoriumResponse;
import com.aditya.movieticketbooking.theater.dto.CreateAuditoriumRequest;
import com.aditya.movieticketbooking.theater.dto.CreateSeatRequest;
import com.aditya.movieticketbooking.theater.dto.SeatResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auditoriums")
public class AuditoriumController {
    private final AuditoriumService auditoriumService;
    private final SeatService seatService;

    public AuditoriumController(AuditoriumService auditoriumService, SeatService seatService) {
        this.auditoriumService = auditoriumService;
        this.seatService = seatService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuditoriumResponse create(@Valid @RequestBody CreateAuditoriumRequest request) {
        return auditoriumService.create(request);
    }

    @PostMapping("/{auditoriumId}/seats")
    @ResponseStatus(HttpStatus.CREATED)
    public SeatResponse createSeat(
            @PathVariable UUID auditoriumId,
            @Valid @RequestBody CreateSeatRequest request) {
        return seatService.create(auditoriumId, request);
    }
}
