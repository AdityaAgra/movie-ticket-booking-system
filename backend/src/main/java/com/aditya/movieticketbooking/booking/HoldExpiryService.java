package com.aditya.movieticketbooking.booking;

import com.aditya.movieticketbooking.show.ShowSeat;
import com.aditya.movieticketbooking.show.ShowSeatRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HoldExpiryService {
    private final ShowSeatRepository showSeatRepository;
    private final BookingRepository bookingRepository;

    public HoldExpiryService(ShowSeatRepository showSeatRepository, BookingRepository bookingRepository) {
        this.showSeatRepository = showSeatRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public int releaseExpiredHolds(Instant now) {
        List<ShowSeat> expiredSeats = showSeatRepository.findExpiredHoldsForUpdate(now);
        if (expiredSeats.isEmpty()) {
            return 0;
        }
        List<java.util.UUID> showSeatIds = expiredSeats.stream().map(ShowSeat::getId).toList();
        bookingRepository.findPendingByShowSeatIds(showSeatIds).forEach(Booking::expire);
        expiredSeats.forEach(ShowSeat::releaseHold);
        return expiredSeats.size();
    }
}
