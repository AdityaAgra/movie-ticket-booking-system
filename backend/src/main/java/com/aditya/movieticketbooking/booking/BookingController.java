package com.aditya.movieticketbooking.booking;

import com.aditya.movieticketbooking.booking.dto.CreateHoldRequest;
import com.aditya.movieticketbooking.booking.dto.HoldResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shows")
public class BookingController {
    private final BookingService bookingService;
    public BookingController(BookingService bookingService) { this.bookingService = bookingService; }

    @PostMapping("/{showId}/holds")
    @ResponseStatus(HttpStatus.CREATED)
    public HoldResponse holdSeats(
            @PathVariable UUID showId,
            @Valid @RequestBody CreateHoldRequest request,
            Authentication authentication) {
        return bookingService.holdSeats(showId, request, authentication.getName());
    }
}
