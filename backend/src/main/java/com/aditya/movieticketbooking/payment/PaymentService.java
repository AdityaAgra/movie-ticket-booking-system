package com.aditya.movieticketbooking.payment;

import com.aditya.movieticketbooking.booking.Booking;
import com.aditya.movieticketbooking.booking.BookingRepository;
import com.aditya.movieticketbooking.booking.BookingSeat;
import com.aditya.movieticketbooking.booking.BookingSeatRepository;
import com.aditya.movieticketbooking.common.enums.BookingStatus;
import com.aditya.movieticketbooking.common.enums.ShowSeatStatus;
import com.aditya.movieticketbooking.common.exception.BusinessConflictException;
import com.aditya.movieticketbooking.common.exception.ResourceNotFoundException;
import com.aditya.movieticketbooking.notification.BookingConfirmedEvent;
import com.aditya.movieticketbooking.payment.dto.PaymentResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentService(
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            PaymentRepository paymentRepository,
            ApplicationEventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PaymentResponse confirmPayment(UUID bookingId, String customerEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        if (!booking.getUser().getEmail().equals(customerEmail)) {
            throw new AccessDeniedException("You do not own this booking.");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessConflictException("Only a pending booking can be paid.");
        }

        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingIdForUpdate(bookingId);
        Instant now = Instant.now();
        boolean validHold = !bookingSeats.isEmpty() && bookingSeats.stream().allMatch(bookingSeat ->
                bookingSeat.getShowSeat().getStatus() == ShowSeatStatus.HELD
                        && bookingSeat.getShowSeat().getHeldByUser().getId().equals(booking.getUser().getId())
                        && bookingSeat.getShowSeat().getHoldExpiry() != null
                        && bookingSeat.getShowSeat().getHoldExpiry().isAfter(now));
        if (!validHold) {
            throw new BusinessConflictException("The seat hold has expired or is no longer valid.");
        }

        Payment payment = paymentRepository.save(Payment.success(booking));
        bookingSeats.forEach(bookingSeat -> bookingSeat.getShowSeat().markBooked());
        booking.confirm();
        eventPublisher.publishEvent(new BookingConfirmedEvent(booking.getId(), booking.getUser().getEmail()));
        return PaymentResponse.from(payment);
    }
}
