package com.aditya.movieticketbooking.refund;

import com.aditya.movieticketbooking.booking.Booking;
import com.aditya.movieticketbooking.booking.BookingRepository;
import com.aditya.movieticketbooking.booking.BookingSeat;
import com.aditya.movieticketbooking.booking.BookingSeatRepository;
import com.aditya.movieticketbooking.common.enums.BookingStatus;
import com.aditya.movieticketbooking.common.enums.ShowSeatStatus;
import com.aditya.movieticketbooking.common.exception.BusinessConflictException;
import com.aditya.movieticketbooking.common.exception.ResourceNotFoundException;
import com.aditya.movieticketbooking.notification.BookingCancellationEvent;
import com.aditya.movieticketbooking.payment.Payment;
import com.aditya.movieticketbooking.payment.PaymentRepository;
import com.aditya.movieticketbooking.refund.dto.CancellationResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingCancellationService {
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PaymentRepository paymentRepository;
    private final RefundService refundService;
    private final ApplicationEventPublisher eventPublisher;

    public BookingCancellationService(
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            PaymentRepository paymentRepository,
            RefundService refundService,
            ApplicationEventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.paymentRepository = paymentRepository;
        this.refundService = refundService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CancellationResponse cancel(UUID bookingId, String customerEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        if (!booking.getUser().getEmail().equals(customerEmail)) {
            throw new AccessDeniedException("You do not own this booking.");
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessConflictException("Only a confirmed booking can be cancelled.");
        }

        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingIdForUpdate(bookingId);
        if (bookingSeats.isEmpty() || bookingSeats.stream().anyMatch(bookingSeat ->
                bookingSeat.getShowSeat().getStatus() != ShowSeatStatus.BOOKED)) {
            throw new BusinessConflictException("Booking seats are not available for cancellation.");
        }
        RefundQuote quote = refundService.calculateRefund(
                booking.getTotalAmount(), booking.getShow().getStartTime(), Instant.now());
        Payment refundPayment = paymentRepository.save(Payment.refund(booking, quote.refundAmount()));
        bookingSeats.forEach(bookingSeat -> bookingSeat.getShowSeat().makeAvailable());
        booking.cancel();
        eventPublisher.publishEvent(new BookingCancellationEvent(
                booking.getId(), booking.getUser().getEmail(), quote.refundAmount()));
        return CancellationResponse.from(refundPayment, quote);
    }
}
