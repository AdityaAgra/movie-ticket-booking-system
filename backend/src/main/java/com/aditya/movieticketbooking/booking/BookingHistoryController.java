package com.aditya.movieticketbooking.booking;

import com.aditya.movieticketbooking.booking.dto.BookingHistoryResponse;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingHistoryController {
    private final BookingHistoryService bookingHistoryService;

    public BookingHistoryController(BookingHistoryService bookingHistoryService) {
        this.bookingHistoryService = bookingHistoryService;
    }

    @GetMapping("/me")
    public List<BookingHistoryResponse> findMyBookings(Authentication authentication) {
        return bookingHistoryService.findMyBookings(authentication.getName());
    }
}
