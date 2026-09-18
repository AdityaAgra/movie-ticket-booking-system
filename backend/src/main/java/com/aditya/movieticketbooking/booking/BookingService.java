package com.aditya.movieticketbooking.booking;

import com.aditya.movieticketbooking.booking.dto.CreateHoldRequest;
import com.aditya.movieticketbooking.booking.dto.HoldResponse;
import com.aditya.movieticketbooking.common.enums.ShowSeatStatus;
import com.aditya.movieticketbooking.common.exception.InvalidRequestException;
import com.aditya.movieticketbooking.common.exception.ResourceNotFoundException;
import com.aditya.movieticketbooking.common.exception.SeatUnavailableException;
import com.aditya.movieticketbooking.pricing.DiscountCode;
import com.aditya.movieticketbooking.pricing.DiscountService;
import com.aditya.movieticketbooking.pricing.PricingService;
import com.aditya.movieticketbooking.show.Show;
import com.aditya.movieticketbooking.show.ShowRepository;
import com.aditya.movieticketbooking.show.ShowSeat;
import com.aditya.movieticketbooking.show.ShowSeatRepository;
import com.aditya.movieticketbooking.user.User;
import com.aditya.movieticketbooking.user.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final UserRepository userRepository;
    private final DiscountService discountService;
    private final PricingService pricingService;
    private final int holdDurationMinutes;

    public BookingService(
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            ShowRepository showRepository,
            ShowSeatRepository showSeatRepository,
            UserRepository userRepository,
            DiscountService discountService,
            PricingService pricingService,
            @Value("${booking.hold-duration-minutes}") int holdDurationMinutes) {
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.userRepository = userRepository;
        this.discountService = discountService;
        this.pricingService = pricingService;
        this.holdDurationMinutes = holdDurationMinutes;
    }

    @Transactional
    public HoldResponse holdSeats(UUID showId, CreateHoldRequest request, String customerEmail) {
        List<UUID> seatIds = request.seatIds();
        if (seatIds.size() != new HashSet<>(seatIds).size()) {
            throw new InvalidRequestException("Each seat may be selected only once.");
        }

        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found."));
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found."));
        List<ShowSeat> showSeats = showSeatRepository.findByShowIdAndSeatIdsForUpdate(showId, seatIds);
        if (showSeats.size() != seatIds.size()) {
            throw new ResourceNotFoundException("One or more selected seats do not belong to this show.");
        }

        Instant now = Instant.now();
        for (ShowSeat showSeat : showSeats) {
            if (showSeat.getStatus() == ShowSeatStatus.HELD
                    && !showSeat.getHoldExpiry().isAfter(now)) {
                showSeat.releaseHold();
            }
            if (showSeat.getStatus() != ShowSeatStatus.AVAILABLE) {
                throw new SeatUnavailableException();
            }
        }

        DiscountCode discountCode = request.discountCode() == null || request.discountCode().isBlank()
                ? null
                : discountService.findValidCode(request.discountCode().trim(), now);
        BigDecimal totalAmount = showSeats.stream()
                .map(showSeat -> pricingService.calculate(
                        show.getBasePrice(),
                        showSeat.getSeat().getSeatType(),
                        show.getStartTime().atZone(ZoneOffset.UTC).toLocalDate(),
                        discountCode).finalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Instant holdExpiresAt = now.plusSeconds(holdDurationMinutes * 60L);
        Booking booking = bookingRepository.save(Booking.pending(customer, show, totalAmount));
        List<BookingSeat> bookingSeats = showSeats.stream()
                .map(showSeat -> BookingSeat.create(booking, showSeat, pricingService.calculate(
                        show.getBasePrice(), showSeat.getSeat().getSeatType(),
                        show.getStartTime().atZone(ZoneOffset.UTC).toLocalDate(), discountCode).finalAmount()))
                .toList();
        bookingSeatRepository.saveAll(bookingSeats);
        showSeats.forEach(showSeat -> showSeat.hold(customer, holdExpiresAt));
        return HoldResponse.from(booking, holdExpiresAt);
    }
}
