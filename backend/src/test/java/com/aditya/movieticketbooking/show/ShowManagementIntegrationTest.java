package com.aditya.movieticketbooking.show;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aditya.movieticketbooking.city.City;
import com.aditya.movieticketbooking.city.CityRepository;
import com.aditya.movieticketbooking.common.enums.SeatType;
import com.aditya.movieticketbooking.common.enums.ShowSeatStatus;
import com.aditya.movieticketbooking.movie.Movie;
import com.aditya.movieticketbooking.movie.MovieRepository;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ShowManagementIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@moviebooking.local";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String CUSTOMER_EMAIL = "customer@moviebooking.local";
    private static final String CUSTOMER_PASSWORD = "customer123";

    @Autowired private MockMvc mockMvc;
    @Autowired private MovieRepository movieRepository;
    @Autowired private ShowRepository showRepository;
    @Autowired private ShowSeatRepository showSeatRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private TheaterRepository theaterRepository;
    @Autowired private AuditoriumRepository auditoriumRepository;
    @Autowired private SeatRepository seatRepository;

    @Test
    void adminCanCreateMovie() throws Exception {
        mockMvc.perform(post("/api/v1/movies")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Interstellar\",\"durationMinutes\":169}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Interstellar"))
                .andExpect(jsonPath("$.durationMinutes").value(169));
    }

    @Test
    void creatingShowGeneratesAvailableShowSeats() throws Exception {
        Movie movie = movieRepository.save(Movie.create("Movie", 120));
        Auditorium auditorium = createAuditoriumWithSeats(3);

        mockMvc.perform(post("/api/v1/shows")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(showRequest(movie.getId(), auditorium.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.showSeatCount").value(3));

        Show show = showRepository.findAll().stream()
                .filter(candidate -> candidate.getMovie().getId().equals(movie.getId()))
                .findFirst()
                .orElseThrow();
        List<ShowSeat> showSeats = showSeatRepository.findByShowId(show.getId());
        assertEquals(3, showSeats.size());
        assertEquals(3, showSeats.stream().filter(seat -> seat.getStatus() == ShowSeatStatus.AVAILABLE).count());
    }

    @Test
    void unknownMovieDoesNotCreateShow() throws Exception {
        Auditorium auditorium = createAuditoriumWithSeats(1);
        long showCountBefore = showRepository.count();

        mockMvc.perform(post("/api/v1/shows")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(showRequest(UUID.randomUUID(), auditorium.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        assertEquals(showCountBefore, showRepository.count());
    }

    @Test
    void unknownAuditoriumDoesNotCreateShow() throws Exception {
        Movie movie = movieRepository.save(Movie.create("Movie", 120));
        long showCountBefore = showRepository.count();

        mockMvc.perform(post("/api/v1/shows")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(showRequest(movie.getId(), UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));

        assertEquals(showCountBefore, showRepository.count());
    }

    @Test
    void duplicateShowSeatIsRejectedByDatabaseConstraint() {
        Movie movie = movieRepository.save(Movie.create("Movie", 120));
        Auditorium auditorium = createAuditoriumWithSeats(1);
        Seat seat = seatRepository.findByAuditoriumId(auditorium.getId()).getFirst();
        Show show = showRepository.save(Show.create(movie, auditorium, Instant.now().plus(2, ChronoUnit.DAYS), new BigDecimal("250.00")));

        showSeatRepository.saveAndFlush(ShowSeat.available(show, seat));

        assertThrows(DataIntegrityViolationException.class,
                () -> showSeatRepository.saveAndFlush(ShowSeat.available(show, seat)));
    }

    @Test
    void customerCannotCreateMoviesOrShows() throws Exception {
        mockMvc.perform(post("/api/v1/movies")
                        .with(httpBasic(CUSTOMER_EMAIL, CUSTOMER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Unauthorized\",\"durationMinutes\":90}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"));
    }

    private Auditorium createAuditoriumWithSeats(int seatCount) {
        City city = cityRepository.save(City.create("City-" + UUID.randomUUID()));
        Theater theater = theaterRepository.save(Theater.create("Theater", "Address", city));
        Auditorium auditorium = auditoriumRepository.save(Auditorium.create("Screen 1", theater));
        for (int number = 1; number <= seatCount; number++) {
            seatRepository.save(Seat.create("A", number, SeatType.REGULAR, auditorium));
        }
        return auditorium;
    }

    private String showRequest(UUID movieId, UUID auditoriumId) {
        return "{\"movieId\":\"" + movieId + "\",\"auditoriumId\":\"" + auditoriumId
                + "\",\"startTime\":\"" + Instant.now().plus(2, ChronoUnit.DAYS)
                + "\",\"basePrice\":250.00}";
    }
}
