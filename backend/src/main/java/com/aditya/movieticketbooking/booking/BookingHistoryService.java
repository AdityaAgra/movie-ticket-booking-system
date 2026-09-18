package com.aditya.movieticketbooking.booking;

import com.aditya.movieticketbooking.booking.dto.BookingHistoryResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingHistoryService {
    private final BookingRepository bookingRepository;

    public BookingHistoryService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<BookingHistoryResponse> findMyBookings(String customerEmail) {
        return bookingRepository.findHistoryByUserEmailOrderByCreatedAtDesc(customerEmail).stream()
                .map(BookingHistoryResponse::from)
                .toList();
    }
}
