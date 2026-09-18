package com.aditya.movieticketbooking.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aditya.movieticketbooking.booking.dto.CreateHoldRequest;
import com.aditya.movieticketbooking.booking.dto.HoldResponse;
import com.aditya.movieticketbooking.city.City;
import com.aditya.movieticketbooking.city.CityRepository;
import com.aditya.movieticketbooking.common.enums.BookingStatus;
import com.aditya.movieticketbooking.common.enums.Role;
import com.aditya.movieticketbooking.common.enums.SeatType;
import com.aditya.movieticketbooking.common.enums.ShowSeatStatus;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HoldExpiryAndHistoryIntegrationTest {
    private static final String CUSTOMER_EMAIL = "customer@moviebooking.local";
    private static final String CUSTOMER_PASSWORD = "customer123";

    @Autowired private MockMvc mockMvc;
    @Autowired private BookingService bookingService;
    @Autowired private HoldExpiryService holdExpiryService;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private TheaterRepository theaterRepository;
    @Autowired private AuditoriumRepository auditoriumRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private ShowRepository showRepository;
    @Autowired private ShowSeatRepository showSeatRepository;

    @Test
    void expiredHeldSeatIsReleasedAndPendingBookingExpires() {
        Fixture fixture = fixture();
        HoldResponse hold = hold(fixture);
        ShowSeat showSeat = showSeatRepository.findByShowId(fixture.show.getId()).getFirst();
        User customer = userRepository.findByEmail(CUSTOMER_EMAIL).orElseThrow();
        showSeat.hold(customer, Instant.now().minus(1, ChronoUnit.MINUTES));
        showSeatRepository.flush();

        assertEquals(1, holdExpiryService.releaseExpiredHolds(Instant.now()));

        assertEquals(ShowSeatStatus.AVAILABLE, showSeat.getStatus());
        assertEquals(null, showSeat.getHeldByUser());
        assertEquals(null, showSeat.getHoldExpiry());
        assertEquals(BookingStatus.EXPIRED, bookingRepository.findById(hold.bookingId()).orElseThrow().getStatus());
    }

    @Test
    void unexpiredHoldIsNotReleased() {
        Fixture fixture = fixture();
        HoldResponse hold = hold(fixture);

        assertEquals(0, holdExpiryService.releaseExpiredHolds(Instant.now()));
        assertEquals(ShowSeatStatus.HELD, showSeatRepository.findByShowId(fixture.show.getId()).getFirst().getStatus());
        assertEquals(BookingStatus.PENDING, bookingRepository.findById(hold.bookingId()).orElseThrow().getStatus());
    }

    @Test
    void bookingHistoryContainsOnlyAuthenticatedCustomersBookingsNewestFirst() throws Exception {
        User historyCustomer = createCustomer("history-owner");
        Fixture firstFixture = fixture();
        HoldResponse firstHold = hold(firstFixture, historyCustomer.getEmail());
        Fixture secondFixture = fixture();
        HoldResponse secondHold = hold(secondFixture, historyCustomer.getEmail());
        User customer = userRepository.findByEmail(CUSTOMER_EMAIL).orElseThrow();
        User otherCustomer = userRepository.save(User.create(
                "Other", "history-" + UUID.randomUUID() + "@example.com", customer.getPasswordHash(), Role.CUSTOMER));
        bookingRepository.save(Booking.pending(otherCustomer, firstFixture.show, new BigDecimal("100.00")));

        mockMvc.perform(get("/api/v1/bookings/me")
                        .with(httpBasic(historyCustomer.getEmail(), CUSTOMER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].bookingId").value(secondHold.bookingId().toString()))
                .andExpect(jsonPath("$[1].bookingId").value(firstHold.bookingId().toString()));
    }

    @Test
    void bookingHistoryIgnoresAttemptedUserIdentityInRequestData() throws Exception {
        User historyCustomer = createCustomer("query-owner");
        Fixture fixture = fixture();
        HoldResponse hold = hold(fixture, historyCustomer.getEmail());
        User customer = userRepository.findByEmail(CUSTOMER_EMAIL).orElseThrow();
        User otherCustomer = userRepository.save(User.create(
                "Other", "query-" + UUID.randomUUID() + "@example.com", customer.getPasswordHash(), Role.CUSTOMER));
        bookingRepository.save(Booking.pending(otherCustomer, fixture.show, new BigDecimal("100.00")));

        mockMvc.perform(get("/api/v1/bookings/me")
                        .queryParam("userEmail", otherCustomer.getEmail())
                        .with(httpBasic(historyCustomer.getEmail(), CUSTOMER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bookingId").value(hold.bookingId().toString()));
    }

    private HoldResponse hold(Fixture fixture) {
        return hold(fixture, CUSTOMER_EMAIL);
    }

    private HoldResponse hold(Fixture fixture, String customerEmail) {
        return bookingService.holdSeats(
                fixture.show.getId(), new CreateHoldRequest(List.of(fixture.seat.getId()), null), customerEmail);
    }

    private User createCustomer(String name) {
        User customer = userRepository.findByEmail(CUSTOMER_EMAIL).orElseThrow();
        return userRepository.save(User.create(
                name, name + "-" + UUID.randomUUID() + "@example.com", customer.getPasswordHash(), Role.CUSTOMER));
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
