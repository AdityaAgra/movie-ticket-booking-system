package com.aditya.movieticketbooking;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aditya.movieticketbooking.common.enums.Role;
import com.aditya.movieticketbooking.user.User;
import com.aditya.movieticketbooking.user.UserRepository;
import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EndToEndBookingFlowIntegrationTest {
    private static final String ADMIN_EMAIL = "admin@moviebooking.local";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String CUSTOMER_PASSWORD = "customer123";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;

    @Test
    void customerCanCompleteCatalogToCancellationFlow() throws Exception {
        String customerEmail = createCustomer();
        String cityId = id(postAsAdmin("/cities", "{\"name\":\"City-" + UUID.randomUUID() + "\"}"));
        String theaterId = id(postAsAdmin("/theaters",
                "{\"name\":\"Theater\",\"address\":\"Address\",\"cityId\":\"" + cityId + "\"}"));
        String auditoriumId = id(postAsAdmin("/auditoriums",
                "{\"name\":\"Screen 1\",\"theaterId\":\"" + theaterId + "\"}"));
        String seatId = id(postAsAdmin("/auditoriums/" + auditoriumId + "/seats",
                "{\"rowLabel\":\"A\",\"seatNumber\":1,\"seatType\":\"REGULAR\"}"));
        String movieId = id(postAsAdmin("/movies", "{\"title\":\"Movie\",\"durationMinutes\":120}"));
        String showId = id(postAsAdmin("/shows",
                "{\"movieId\":\"" + movieId + "\",\"auditoriumId\":\"" + auditoriumId
                        + "\",\"startTime\":\"" + Instant.now().plus(48, ChronoUnit.HOURS)
                        + "\",\"basePrice\":250.00}"));
        postAsAdmin("/refund-policies",
                "{\"minimumHoursBeforeShow\":0,\"refundPercentage\":100.00,\"active\":true}");

        mockMvc.perform(get("/api/v1/shows")
                        .queryParam("cityId", cityId)
                        .queryParam("date", Instant.now().plus(48, ChronoUnit.HOURS).toString().substring(0, 10))
                        .with(httpBasic(customerEmail, CUSTOMER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(showId));

        MvcResult holdResult = mockMvc.perform(post("/api/v1/shows/{showId}/holds", showId)
                        .with(httpBasic(customerEmail, CUSTOMER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"seatIds\":[\"" + seatId + "\"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();
        String bookingId = JsonPath.read(holdResult.getResponse().getContentAsString(), "$.bookingId");

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/pay", bookingId)
                        .with(httpBasic(customerEmail, CUSTOMER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingStatus").value("CONFIRMED"));

        mockMvc.perform(get("/api/v1/bookings/me").with(httpBasic(customerEmail, CUSTOMER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingId").value(bookingId))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));

        mockMvc.perform(post("/api/v1/bookings/{bookingId}/cancel", bookingId)
                        .with(httpBasic(customerEmail, CUSTOMER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingStatus").value("CANCELLED"))
                .andExpect(jsonPath("$.refundPaymentStatus").value("REFUNDED"));

        mockMvc.perform(get("/api/v1/shows/{showId}/seats", showId)
                        .with(httpBasic(customerEmail, CUSTOMER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
        mockMvc.perform(get("/api/v1/bookings/me").with(httpBasic(customerEmail, CUSTOMER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CANCELLED"));
    }

    private MvcResult postAsAdmin(String path, String body) throws Exception {
        return mockMvc.perform(post("/api/v1" + path)
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private String id(MvcResult result) throws Exception {
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private String createCustomer() {
        User bootstrapCustomer = userRepository.findByEmail("customer@moviebooking.local").orElseThrow();
        String email = "e2e-" + UUID.randomUUID() + "@example.com";
        userRepository.save(User.create("E2E Customer", email, bootstrapCustomer.getPasswordHash(), Role.CUSTOMER));
        return email;
    }
}
