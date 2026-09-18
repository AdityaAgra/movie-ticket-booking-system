package com.aditya.movieticketbooking.pricing;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
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
class PricingAdministrationIntegrationTest {
    private static final String ADMIN_EMAIL = "admin@moviebooking.local";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String CUSTOMER_EMAIL = "customer@moviebooking.local";
    private static final String CUSTOMER_PASSWORD = "customer123";

    @Autowired private MockMvc mockMvc;

    @Test
    void adminCanCreateDiscountCode() throws Exception {
        mockMvc.perform(post("/api/v1/discount-codes")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(discountBody("weekend10", Instant.now().minusSeconds(60), Instant.now().plusSeconds(3_600))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("WEEKEND10"))
                .andExpect(jsonPath("$.discountType").value("PERCENTAGE"));
    }

    @Test
    void invalidDiscountDateRangeReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/discount-codes")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(discountBody("INVALID", Instant.now().plusSeconds(3_600), Instant.now())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    void adminCanCreateRefundPolicy() throws Exception {
        mockMvc.perform(post("/api/v1/refund-policies")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"minimumHoursBeforeShow\":24,\"refundPercentage\":100.00,\"active\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.minimumHoursBeforeShow").value(24))
                .andExpect(jsonPath("$.refundPercentage").value(100.00));
    }

    @Test
    void customerCannotConfigurePricingOrRefunds() throws Exception {
        mockMvc.perform(post("/api/v1/discount-codes")
                        .with(httpBasic(CUSTOMER_EMAIL, CUSTOMER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(discountBody("NOACCESS", Instant.now().minusSeconds(60), Instant.now().plusSeconds(3_600))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"));
    }

    private String discountBody(String code, Instant validFrom, Instant validTo) {
        return "{\"code\":\"" + code + "\",\"discountType\":\"PERCENTAGE\",\"value\":10.00,\"validFrom\":\""
                + validFrom + "\",\"validTo\":\"" + validTo + "\",\"active\":true}";
    }
}
