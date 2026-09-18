package com.aditya.movieticketbooking.show;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ShowDiscoveryIntegrationTest {

    private static final String CUSTOMER_EMAIL = "customer@moviebooking.local";
    private static final String CUSTOMER_PASSWORD = "customer123";

    @Autowired private MockMvc mockMvc;
    @Autowired private CityRepository cityRepository;
    @Autowired private TheaterRepository theaterRepository;
    @Autowired private AuditoriumRepository auditoriumRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private ShowRepository showRepository;
    @Autowired private ShowSeatRepository showSeatRepository;

    @Test
    void authenticatedCustomerCanBrowseCities() throws Exception {
        String cityName = "City-" + UUID.randomUUID();
        cityRepository.save(City.create(cityName));

        mockMvc.perform(get("/api/v1/cities").with(httpBasic(CUSTOMER_EMAIL, CUSTOMER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == '" + cityName + "')]").exists());
    }

    @Test
    void showSearchReturnsOnlyShowsForRequestedCityAndDate() throws Exception {
        LocalDate requestedDate = LocalDate.now(ZoneOffset.UTC).plusDays(5);
        City city = cityRepository.save(City.create("City-" + UUID.randomUUID()));
        Auditorium auditorium = createAuditorium(city, 1);
        Movie movie = movieRepository.save(Movie.create("Requested Movie", 120));
        Show matchingShow = showRepository.save(Show.create(
                movie, auditorium, requestedDate.atTime(18, 0).toInstant(ZoneOffset.UTC), new BigDecimal("250.00")));
        showRepository.save(Show.create(
                movie, auditorium, requestedDate.plusDays(1).atTime(18, 0).toInstant(ZoneOffset.UTC), new BigDecimal("250.00")));

        mockMvc.perform(get("/api/v1/shows")
                        .with(httpBasic(CUSTOMER_EMAIL, CUSTOMER_PASSWORD))
                        .param("cityId", city.getId().toString())
                        .param("date", requestedDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(matchingShow.getId().toString()))
                .andExpect(jsonPath("$[0].movieTitle").value("Requested Movie"));
    }

    @Test
    void seatMapReturnsPhysicalSeatDetailsAndAvailability() throws Exception {
        City city = cityRepository.save(City.create("City-" + UUID.randomUUID()));
        Auditorium auditorium = createAuditorium(city, 2);
        Movie movie = movieRepository.save(Movie.create("Movie", 120));
        Show show = showRepository.save(Show.create(
                movie, auditorium, Instant.now().plusSeconds(86_400), new BigDecimal("250.00")));
        seatRepository.findByAuditoriumId(auditorium.getId())
                .forEach(seat -> showSeatRepository.save(ShowSeat.available(show, seat)));

        mockMvc.perform(get("/api/v1/shows/{showId}/seats", show.getId())
                        .with(httpBasic(CUSTOMER_EMAIL, CUSTOMER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].rowLabel").value("A"))
                .andExpect(jsonPath("$[0].seatNumber").value(1))
                .andExpect(jsonPath("$[0].seatType").value("REGULAR"))
                .andExpect(jsonPath("$[0].status").value(ShowSeatStatus.AVAILABLE.name()));
    }

    @Test
    void unknownShowSeatMapReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/shows/{showId}/seats", UUID.randomUUID())
                        .with(httpBasic(CUSTOMER_EMAIL, CUSTOMER_PASSWORD)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void unauthenticatedCustomerDiscoveryRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/cities"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    private Auditorium createAuditorium(City city, int seatCount) {
        Theater theater = theaterRepository.save(Theater.create("Theater", "Address", city));
        Auditorium auditorium = auditoriumRepository.save(Auditorium.create("Screen 1", theater));
        for (int number = 1; number <= seatCount; number++) {
            seatRepository.save(Seat.create("A", number, SeatType.REGULAR, auditorium));
        }
        return auditorium;
    }
}
