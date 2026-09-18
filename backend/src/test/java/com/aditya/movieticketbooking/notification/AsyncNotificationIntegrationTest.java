package com.aditya.movieticketbooking.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aditya.movieticketbooking.booking.BookingService;
import com.aditya.movieticketbooking.booking.dto.CreateHoldRequest;
import com.aditya.movieticketbooking.booking.dto.HoldResponse;
import com.aditya.movieticketbooking.city.City;
import com.aditya.movieticketbooking.city.CityRepository;
import com.aditya.movieticketbooking.common.enums.BookingStatus;
import com.aditya.movieticketbooking.common.enums.SeatType;
import com.aditya.movieticketbooking.movie.Movie;
import com.aditya.movieticketbooking.movie.MovieRepository;
import com.aditya.movieticketbooking.payment.PaymentService;
import com.aditya.movieticketbooking.payment.dto.PaymentResponse;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "notification.simulated-delay-millis=1500")
class AsyncNotificationIntegrationTest {
    private static final String CUSTOMER_EMAIL = "customer@moviebooking.local";

    @Autowired private BookingService bookingService;
    @Autowired private PaymentService paymentService;
    @Autowired private CityRepository cityRepository;
    @Autowired private TheaterRepository theaterRepository;
    @Autowired private AuditoriumRepository auditoriumRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private ShowRepository showRepository;
    @Autowired private ShowSeatRepository showSeatRepository;

    @Test
    void paymentCompletesWithoutWaitingForSlowNotification() {
        Fixture fixture = fixture();
        HoldResponse hold = bookingService.holdSeats(
                fixture.show.getId(), new CreateHoldRequest(List.of(fixture.seat.getId()), null), CUSTOMER_EMAIL);

        long startedAt = System.nanoTime();
        PaymentResponse payment = paymentService.confirmPayment(hold.bookingId(), CUSTOMER_EMAIL);
        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;

        assertEquals(BookingStatus.CONFIRMED, payment.bookingStatus());
        assertTrue(elapsedMillis < 1000, "Payment should not wait for the 1500ms notification delay.");
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
