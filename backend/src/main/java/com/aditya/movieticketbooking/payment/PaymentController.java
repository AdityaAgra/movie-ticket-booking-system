package com.aditya.movieticketbooking.payment;

import com.aditya.movieticketbooking.payment.dto.PaymentResponse;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
public class PaymentController {
    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) { this.paymentService = paymentService; }

    @PostMapping("/{bookingId}/pay")
    public PaymentResponse pay(@PathVariable UUID bookingId, Authentication authentication) {
        return paymentService.confirmPayment(bookingId, authentication.getName());
    }
}
