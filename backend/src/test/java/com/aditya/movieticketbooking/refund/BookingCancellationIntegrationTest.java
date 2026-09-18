package com.aditya.movieticketbooking.refund;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aditya.movieticketbooking.booking.Booking;
import com.aditya.movieticketbooking.booking.BookingRepository;
import com.aditya.movieticketbooking.booking.BookingService;
import com.aditya.movieticketbooking.booking.dto.CreateHoldRequest;
import com.aditya.movieticketbooking.booking.dto.HoldResponse;
import com.aditya.movieticketbooking.city.City;
import com.aditya.movieticketbooking.city.CityRepository;
import com.aditya.movieticketbooking.common.enums.BookingStatus;
import com.aditya.movieticketbooking.common.enums.PaymentStatus;
import com.aditya.movieticketbooking.common.enums.Role;
import com.aditya.movieticketbooking.common.enums.SeatType;
import com.aditya.movieticketbooking.common.enums.ShowSeatStatus;
import com.aditya.movieticketbooking.common.exception.BusinessConflictException;
import com.aditya.movieticketbooking.movie.Movie;
import com.aditya.movieticketbooking.movie.MovieRepository;
import com.aditya.movieticketbooking.payment.Payment;
import com.aditya.movieticketbooking.payment.PaymentRepository;
import com.aditya.movieticketbooking.payment.PaymentService;
import com.aditya.movieticketbooking.show.Show;
import com.aditya.movieticketbooking.show.ShowRepository;
import com.aditya.movieticketbooking.show.ShowSeat;
import com.aditya.movieticketbooking.show.ShowSeatRepository;
import com.aditya.movieticketbooking.theater.Auditorium;
import com.aditya.movieticketbooking.theater.AuditoriumRepository;
import com.aditya.movieticketbooking.theater.Seat;
import com.aditya.movieticketbooking.theater.SeatRepository;
import com.aditya.movieticketbooking.theater.Theater;
import com.aditya.movieticketbooking.theater.TheaterRepository;
import com.aditya.movieticketbooking.user.User;
import com.aditya.movieticketbooking.user.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookingCancellationIntegrationTest {
    private static final String CUSTOMER_EMAIL = "customer@moviebooking.local";
    private static final String CUSTOMER_PASSWORD = "customer123";

    @Autowired private MockMvc mockMvc;
    @Autowired private BookingService bookingService;
    @Autowired private BookingCancellationService bookingCancellationService;
    @Autowired private PaymentService paymentService;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private RefundPolicyRepository refundPolicyRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private TheaterRepository theaterRepository;
    @Autowired private AuditoriumRepository auditoriumRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private ShowRepository showRepository;
    @Autowired private ShowSeatRepository showSeatRepository;

    @Test
    void ownerCancelsConfirmedBookingAndReceivesRefund() throws Exception {
        policy(0, "50.00");
        ConfirmedBooking confirmed = confirmedBooking(48);
        BigDecimal expectedRefund = confirmed.booking.getTotalAmount()
                .multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP);

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/cancel", confirmed.booking.getId())
                        .with(httpBasic(CUSTOMER_EMAIL, CUSTOMER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingStatus").value("CANCELLED"))
                .andExpect(jsonPath("$.refundPaymentStatus").value("REFUNDED"))
                .andExpect(jsonPath("$.refundPercentage").value(50.00));

        assertEquals(BookingStatus.CANCELLED, confirmed.booking.getStatus());
        List<Payment> payments = paymentRepository.findByBookingIdOrderByCreatedAtAsc(confirmed.booking.getId());
        assertEquals(2, payments.size());
        assertEquals(PaymentStatus.REFUNDED, payments.getLast().getStatus());
        assertEquals(expectedRefund, payments.getLast().getAmount());
    }

    @Test
    void cancellationUsesBestPolicyForEachLeadTime() {
        policy(24, "100.00");
        policy(2, "50.00");
        policy(0, "0.00");

        ConfirmedBooking dayAhead = confirmedBooking(48);
        ConfirmedBooking hoursAhead = confirmedBooking(4);
        ConfirmedBooking closeToShow = confirmedBooking(1);

        assertEquals(new BigDecimal("100.00"),
                bookingCancellationService.cancel(dayAhead.booking.getId(), CUSTOMER_EMAIL).refundPercentage());
        assertEquals(new BigDecimal("50.00"),
                bookingCancellationService.cancel(hoursAhead.booking.getId(), CUSTOMER_EMAIL).refundPercentage());
        assertEquals(new BigDecimal("0.00"),
                bookingCancellationService.cancel(closeToShow.booking.getId(), CUSTOMER_EMAIL).refundPercentage());
    }

    @Test
    void pendingExpiredAndCancelledBookingsCannotBeCancelledAgain() {
        policy(0, "100.00");
        Fixture pendingFixture = fixture(48);
        HoldResponse pendingHold = bookingService.holdSeats(
                pendingFixture.show.getId(), new CreateHoldRequest(List.of(pendingFixture.seat.getId()), null), CUSTOMER_EMAIL);
        assertThrows(BusinessConflictException.class,
                () -> bookingCancellationService.cancel(pendingHold.bookingId(), CUSTOMER_EMAIL));

        ConfirmedBooking expired = confirmedBooking(48);
        expired.booking.expire();
        assertThrows(BusinessConflictException.class,
                () -> bookingCancellationService.cancel(expired.booking.getId(), CUSTOMER_EMAIL));

        ConfirmedBooking cancelled = confirmedBooking(48);
        bookingCancellationService.cancel(cancelled.booking.getId(), CUSTOMER_EMAIL);
        assertThrows(BusinessConflictException.class,
                () -> bookingCancellationService.cancel(cancelled.booking.getId(), CUSTOMER_EMAIL));
    }

    @Test
    void anotherCustomerCannotCancelBooking() {
        policy(0, "100.00");
        ConfirmedBooking confirmed = confirmedBooking(48);
        User customer = userRepository.findByEmail(CUSTOMER_EMAIL).orElseThrow();
        User otherCustomer = userRepository.save(User.create(
                "Other", "cancel-" + UUID.randomUUID() + "@example.com", customer.getPasswordHash(), Role.CUSTOMER));

        assertThrows(AccessDeniedException.class,
                () -> bookingCancellationService.cancel(confirmed.booking.getId(), otherCustomer.getEmail()));
        assertEquals(BookingStatus.CONFIRMED, confirmed.booking.getStatus());
        assertEquals(1, paymentRepository.countByBookingId(confirmed.booking.getId()));
    }

    @Test
    void cancellationReleasesSeatsForResale() {
        policy(0, "100.00");
        ConfirmedBooking confirmed = confirmedBooking(48);

        bookingCancellationService.cancel(confirmed.booking.getId(), CUSTOMER_EMAIL);

        ShowSeat showSeat = showSeatRepository.findByShowId(confirmed.fixture.show.getId()).getFirst();
        assertEquals(ShowSeatStatus.AVAILABLE, showSeat.getStatus());
        assertEquals(null, showSeat.getHeldByUser());
        assertEquals(null, showSeat.getHoldExpiry());
    }

    private ConfirmedBooking confirmedBooking(long hoursUntilShow) {
        Fixture fixture = fixture(hoursUntilShow);
        HoldResponse hold = bookingService.holdSeats(
                fixture.show.getId(), new CreateHoldRequest(List.of(fixture.seat.getId()), null), CUSTOMER_EMAIL);
        paymentService.confirmPayment(hold.bookingId(), CUSTOMER_EMAIL);
        return new ConfirmedBooking(bookingRepository.findById(hold.bookingId()).orElseThrow(), fixture);
    }

    private void policy(int minimumHoursBeforeShow, String refundPercentage) {
        refundPolicyRepository.save(RefundPolicy.create(
                minimumHoursBeforeShow, new BigDecimal(refundPercentage), true));
    }

    private Fixture fixture(long hoursUntilShow) {
        City city = cityRepository.save(City.create("City-" + UUID.randomUUID()));
        Theater theater = theaterRepository.save(Theater.create("Theater", "Address", city));
        Auditorium auditorium = auditoriumRepository.save(Auditorium.create("Screen", theater));
        Seat seat = seatRepository.save(Seat.create("A", 1, SeatType.REGULAR, auditorium));
        Movie movie = movieRepository.save(Movie.create("Movie", 120));
        Show show = showRepository.save(Show.create(movie, auditorium,
                Instant.now().plus(hoursUntilShow, ChronoUnit.HOURS), new BigDecimal("250.00")));
        showSeatRepository.saveAndFlush(ShowSeat.available(show, seat));
        return new Fixture(show, seat);
    }

    private record Fixture(Show show, Seat seat) { }
    private record ConfirmedBooking(Booking booking, Fixture fixture) { }
}
