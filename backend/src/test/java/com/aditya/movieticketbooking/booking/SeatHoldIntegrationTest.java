package com.aditya.movieticketbooking.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aditya.movieticketbooking.booking.dto.CreateHoldRequest;
import com.aditya.movieticketbooking.city.City;
import com.aditya.movieticketbooking.city.CityRepository;
import com.aditya.movieticketbooking.common.enums.Role;
import com.aditya.movieticketbooking.common.enums.SeatType;
import com.aditya.movieticketbooking.common.enums.ShowSeatStatus;
import com.aditya.movieticketbooking.common.exception.SeatUnavailableException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SeatHoldIntegrationTest {
    private static final String CUSTOMER_EMAIL = "customer@moviebooking.local";
    private static final String CUSTOMER_PASSWORD = "customer123";
    private static final String ADMIN_EMAIL = "admin@moviebooking.local";
    private static final String ADMIN_PASSWORD = "admin123";

    @Autowired private MockMvc mockMvc;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private BookingSeatRepository bookingSeatRepository;
    @Autowired private BookingService bookingService;
    @Autowired private UserRepository userRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private TheaterRepository theaterRepository;
    @Autowired private AuditoriumRepository auditoriumRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private ShowRepository showRepository;
    @Autowired private ShowSeatRepository showSeatRepository;

    @Test
    void customerCanHoldOneAvailableSeat() throws Exception {
        Fixture fixture = fixture(1);

        mockMvc.perform(hold(fixture.show.getId(), List.of(fixture.seats.getFirst().getId()), CUSTOMER_EMAIL, CUSTOMER_PASSWORD))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));

        assertEquals(1, bookingRepository.countByShowId(fixture.show.getId()));
        assertEquals(ShowSeatStatus.HELD, showSeatRepository.findByShowId(fixture.show.getId()).getFirst().getStatus());
    }

    @Test
    void customerCanHoldMultipleAvailableSeatsInOneBooking() throws Exception {
        Fixture fixture = fixture(2);

        mockMvc.perform(hold(fixture.show.getId(), fixture.seatIds(), CUSTOMER_EMAIL, CUSTOMER_PASSWORD))
                .andExpect(status().isCreated());

        assertEquals(1, bookingRepository.countByShowId(fixture.show.getId()));
        assertEquals(2, bookingSeatRepository.countByBookingShowId(fixture.show.getId()));
        assertEquals(2, showSeatRepository.findByShowId(fixture.show.getId()).stream()
                .filter(showSeat -> showSeat.getStatus() == ShowSeatStatus.HELD).count());
    }

    @Test
    void unknownSeatDoesNotCreatePartialHold() throws Exception {
        Fixture fixture = fixture(1);

        mockMvc.perform(hold(fixture.show.getId(), List.of(fixture.seats.getFirst().getId(), UUID.randomUUID()), CUSTOMER_EMAIL, CUSTOMER_PASSWORD))
                .andExpect(status().isNotFound());

        assertEquals(0, bookingRepository.countByShowId(fixture.show.getId()));
        assertEquals(ShowSeatStatus.AVAILABLE, showSeatRepository.findByShowId(fixture.show.getId()).getFirst().getStatus());
    }

    @Test
    void bookedSeatReturnsConflictWithoutChangingOtherRequestedSeat() throws Exception {
        Fixture fixture = fixture(2);
        ShowSeat booked = showSeatRepository.findByShowId(fixture.show.getId()).getFirst();
        booked.markBooked();
        showSeatRepository.flush();

        mockMvc.perform(hold(fixture.show.getId(), fixture.seatIds(), CUSTOMER_EMAIL, CUSTOMER_PASSWORD))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("SEAT_UNAVAILABLE"));

        assertEquals(0, bookingRepository.countByShowId(fixture.show.getId()));
        assertTrue(showSeatRepository.findByShowId(fixture.show.getId()).stream()
                .anyMatch(showSeat -> showSeat.getStatus() == ShowSeatStatus.AVAILABLE));
    }

    @Test
    void secondCustomerCannotHoldAnActiveHold() throws Exception {
        Fixture fixture = fixture(1);
        mockMvc.perform(hold(fixture.show.getId(), fixture.seatIds(), CUSTOMER_EMAIL, CUSTOMER_PASSWORD))
                .andExpect(status().isCreated());
        User customer = userRepository.findByEmail(CUSTOMER_EMAIL).orElseThrow();
        User anotherCustomer = userRepository.save(User.create(
                "Another", "another-" + UUID.randomUUID() + "@example.com", customer.getPasswordHash(), Role.CUSTOMER));

        assertTrue(anotherCustomer.getId() != null);
        assertThrowsSeatUnavailable(() -> bookingService.holdSeats(
                fixture.show.getId(), new CreateHoldRequest(fixture.seatIds(), null), anotherCustomer.getEmail()));
        assertEquals(1, bookingRepository.countByShowId(fixture.show.getId()));
    }

    @Test
    void expiredHoldCanBeTakenByAnotherCustomer() throws Exception {
        Fixture fixture = fixture(1);
        User firstCustomer = userRepository.findByEmail(CUSTOMER_EMAIL).orElseThrow();
        User anotherCustomer = userRepository.save(User.create(
                "Another", "expired-" + UUID.randomUUID() + "@example.com", firstCustomer.getPasswordHash(), Role.CUSTOMER));
        ShowSeat showSeat = showSeatRepository.findByShowId(fixture.show.getId()).getFirst();
        showSeat.hold(firstCustomer, Instant.now().minus(1, ChronoUnit.MINUTES));
        showSeatRepository.flush();

        bookingService.holdSeats(fixture.show.getId(), new CreateHoldRequest(fixture.seatIds(), null), anotherCustomer.getEmail());

        ShowSeat heldAgain = showSeatRepository.findByShowId(fixture.show.getId()).getFirst();
        assertEquals(ShowSeatStatus.HELD, heldAgain.getStatus());
        assertEquals(anotherCustomer.getId(), heldAgain.getHeldByUser().getId());
    }

    // This is not transactional: each competing call must use its own database transaction.
    @RepeatedTest(3)
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void concurrentHoldsHaveExactlyOneSuccess() throws Exception {
        Fixture fixture = fixture(1);
        User customer = userRepository.findByEmail(CUSTOMER_EMAIL).orElseThrow();
        User anotherCustomer = userRepository.saveAndFlush(User.create(
                "Concurrent", "concurrent-" + UUID.randomUUID() + "@example.com", customer.getPasswordHash(), Role.CUSTOMER));
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (String email : List.of(customer.getEmail(), anotherCustomer.getEmail())) {
                results.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        bookingService.holdSeats(fixture.show.getId(), new CreateHoldRequest(fixture.seatIds(), null), email);
                        return true;
                    } catch (SeatUnavailableException exception) {
                        return false;
                    }
                }));
            }
            ready.await();
            start.countDown();
            long successes = results.stream().filter(result -> {
                try { return result.get(); } catch (Exception exception) { throw new AssertionError(exception); }
            }).count();
            assertEquals(1, successes);
        } finally {
            executor.shutdownNow();
        }
    }

    private void assertThrowsSeatUnavailable(Runnable action) {
        try {
            action.run();
        } catch (SeatUnavailableException exception) {
            return;
        }
        throw new AssertionError("Expected SeatUnavailableException");
    }

    private Fixture fixture(int seatCount) {
        City city = cityRepository.save(City.create("City-" + UUID.randomUUID()));
        Theater theater = theaterRepository.save(Theater.create("Theater", "Address", city));
        Auditorium auditorium = auditoriumRepository.save(Auditorium.create("Screen", theater));
        List<Seat> seats = new ArrayList<>();
        for (int number = 1; number <= seatCount; number++) {
            seats.add(seatRepository.save(Seat.create("A", number, SeatType.REGULAR, auditorium)));
        }
        Movie movie = movieRepository.save(Movie.create("Movie", 120));
        Show show = showRepository.save(Show.create(movie, auditorium,
                Instant.now().plus(2, ChronoUnit.DAYS), new BigDecimal("250.00")));
        showSeatRepository.saveAll(seats.stream().map(seat -> ShowSeat.available(show, seat)).toList());
        showSeatRepository.flush();
        return new Fixture(show, seats);
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder hold(
            UUID showId, List<UUID> seatIds, String email, String password) {
        String seats = seatIds.stream().map(id -> "\"" + id + "\"").reduce((left, right) -> left + "," + right).orElseThrow();
        return post("/api/v1/shows/{showId}/holds", showId)
                .with(httpBasic(email, password))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"seatIds\":[" + seats + "]}");
    }

    private record Fixture(Show show, List<Seat> seats) {
        List<UUID> seatIds() { return seats.stream().map(Seat::getId).toList(); }
    }
}
