package com.aditya.movieticketbooking.payment;

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
import com.aditya.movieticketbooking.common.enums.Role;
import com.aditya.movieticketbooking.common.enums.SeatType;
import com.aditya.movieticketbooking.common.enums.ShowSeatStatus;
import com.aditya.movieticketbooking.common.exception.BusinessConflictException;
import com.aditya.movieticketbooking.movie.Movie;
import com.aditya.movieticketbooking.movie.MovieRepository;
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
class PaymentIntegrationTest {
    private static final String CUSTOMER_EMAIL = "customer@moviebooking.local";
    private static final String CUSTOMER_PASSWORD = "customer123";

    @Autowired private MockMvc mockMvc;
    @Autowired private BookingService bookingService;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private PaymentService paymentService;
    @Autowired private ShowSeatRepository showSeatRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private TheaterRepository theaterRepository;
    @Autowired private AuditoriumRepository auditoriumRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private ShowRepository showRepository;

    @Test
    void customerCanPayForAValidPendingHold() throws Exception {
        Fixture fixture = fixture();
        HoldResponse hold = hold(fixture);

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/pay", hold.bookingId())
                        .with(httpBasic(CUSTOMER_EMAIL, CUSTOMER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.bookingStatus").value("CONFIRMED"));

        assertEquals(1, paymentRepository.countByBookingId(hold.bookingId()));
        assertEquals(BookingStatus.CONFIRMED, bookingRepository.findById(hold.bookingId()).orElseThrow().getStatus());
        ShowSeat showSeat = showSeatRepository.findByShowId(fixture.show.getId()).getFirst();
        assertEquals(ShowSeatStatus.BOOKED, showSeat.getStatus());
        assertEquals(null, showSeat.getHeldByUser());
        assertEquals(null, showSeat.getHoldExpiry());
    }

    @Test
    void expiredHoldCannotBePaid() throws Exception {
        Fixture fixture = fixture();
        HoldResponse hold = hold(fixture);
        ShowSeat showSeat = showSeatRepository.findByShowId(fixture.show.getId()).getFirst();
        User customer = userRepository.findByEmail(CUSTOMER_EMAIL).orElseThrow();
        showSeat.hold(customer, Instant.now().minus(1, ChronoUnit.MINUTES));
        showSeatRepository.flush();

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/pay", hold.bookingId())
                        .with(httpBasic(CUSTOMER_EMAIL, CUSTOMER_PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));

        assertEquals(0, paymentRepository.countByBookingId(hold.bookingId()));
        assertEquals(BookingStatus.PENDING, bookingRepository.findById(hold.bookingId()).orElseThrow().getStatus());
    }

    @Test
    void anotherCustomerCannotPaySomeoneElsesBooking() {
        Fixture fixture = fixture();
        HoldResponse hold = hold(fixture);
        User customer = userRepository.findByEmail(CUSTOMER_EMAIL).orElseThrow();
        User anotherCustomer = userRepository.save(User.create(
                "Another", "payment-" + UUID.randomUUID() + "@example.com", customer.getPasswordHash(), Role.CUSTOMER));

        assertThrows(AccessDeniedException.class,
                () -> paymentService.confirmPayment(hold.bookingId(), anotherCustomer.getEmail()));
        assertEquals(0, paymentRepository.countByBookingId(hold.bookingId()));
    }

    @Test
    void alreadyConfirmedBookingCannotBePaidTwice() {
        Fixture fixture = fixture();
        HoldResponse hold = hold(fixture);
        paymentService.confirmPayment(hold.bookingId(), CUSTOMER_EMAIL);

        assertThrows(BusinessConflictException.class,
                () -> paymentService.confirmPayment(hold.bookingId(), CUSTOMER_EMAIL));
        assertEquals(1, paymentRepository.countByBookingId(hold.bookingId()));
    }

    private HoldResponse hold(Fixture fixture) {
        return bookingService.holdSeats(
                fixture.show.getId(), new CreateHoldRequest(List.of(fixture.seat.getId()), null), CUSTOMER_EMAIL);
    }

    private Fixture fixture() {
        City city = cityRepository.save(City.create("City-" + UUID.randomUUID()));
        Theater theater = theaterRepository.save(Theater.create("Theater", "Address", city));
        Auditorium auditorium = auditoriumRepository.save(Auditorium.create("Screen", theater));
        Seat seat = seatRepository.save(Seat.create("A", 1, SeatType.REGULAR, auditorium));
        Movie movie = movieRepository.save(Movie.create("Movie", 120));
        Show show = showRepository.save(Show.create(movie, auditorium,
                Instant.now().plus(2, ChronoUnit.DAYS), new BigDecimal("250.00")));
        showSeatRepository.saveAndFlush(ShowSeat.available(show, seat));
        return new Fixture(show, seat);
    }

    private record Fixture(Show show, Seat seat) { }
}
