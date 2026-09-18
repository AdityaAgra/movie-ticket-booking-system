package com.aditya.movieticketbooking.theater;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aditya.movieticketbooking.city.City;
import com.aditya.movieticketbooking.city.CityRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VenueManagementIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@moviebooking.local";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String CUSTOMER_EMAIL = "customer@moviebooking.local";
    private static final String CUSTOMER_PASSWORD = "customer123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private AuditoriumRepository auditoriumRepository;

    @Test
    void adminCanCreateCity() throws Exception {
        String cityName = "City-" + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/cities")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + cityName + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(cityName));
    }

    @Test
    void adminCanCreateTheaterForExistingCity() throws Exception {
        City city = cityRepository.save(City.create("City-" + UUID.randomUUID()));

        mockMvc.perform(post("/api/v1/theaters")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Grand Cinema\",\"address\":\"Main Street\",\"cityId\":\""
                                + city.getId() + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cityId").value(city.getId().toString()));
    }

    @Test
    void theaterWithUnknownCityReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/theaters")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Grand Cinema\",\"address\":\"Main Street\",\"cityId\":\""
                                + UUID.randomUUID() + "\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void adminCanAddPhysicalSeatToAuditorium() throws Exception {
        Auditorium auditorium = createAuditorium();

        mockMvc.perform(post("/api/v1/auditoriums/{id}/seats", auditorium.getId())
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rowLabel\":\"A\",\"seatNumber\":1,\"seatType\":\"PREMIUM\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rowLabel").value("A"))
                .andExpect(jsonPath("$.seatNumber").value(1))
                .andExpect(jsonPath("$.seatType").value("PREMIUM"));
    }

    @Test
    void duplicateSeatPositionReturnsConflict() throws Exception {
        Auditorium auditorium = createAuditorium();
        String body = "{\"rowLabel\":\"A\",\"seatNumber\":1,\"seatType\":\"REGULAR\"}";

        mockMvc.perform(post("/api/v1/auditoriums/{id}/seats", auditorium.getId())
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auditoriums/{id}/seats", auditorium.getId())
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void customerCannotCreateVenueInventory() throws Exception {
        mockMvc.perform(post("/api/v1/cities")
                        .with(httpBasic(CUSTOMER_EMAIL, CUSTOMER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Unauthorized City\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"));
    }

    private Auditorium createAuditorium() {
        City city = cityRepository.save(City.create("City-" + UUID.randomUUID()));
        Theater theater = theaterRepository.save(Theater.create("Theater", "Address", city));
        return auditoriumRepository.save(Auditorium.create("Screen 1", theater));
    }
}
