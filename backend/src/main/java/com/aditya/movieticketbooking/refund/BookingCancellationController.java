package com.aditya.movieticketbooking.refund;

import com.aditya.movieticketbooking.refund.dto.CancellationResponse;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingCancellationController {
    private final BookingCancellationService bookingCancellationService;

    public BookingCancellationController(BookingCancellationService bookingCancellationService) {
        this.bookingCancellationService = bookingCancellationService;
    }

    @PostMapping("/{bookingId}/cancel")
    public CancellationResponse cancel(@PathVariable UUID bookingId, Authentication authentication) {
        return bookingCancellationService.cancel(bookingId, authentication.getName());
    }
}
