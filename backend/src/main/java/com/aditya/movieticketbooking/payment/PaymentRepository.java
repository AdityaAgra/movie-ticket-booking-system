package com.aditya.movieticketbooking.payment;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    long countByBookingId(UUID bookingId);
    List<Payment> findByBookingIdOrderByCreatedAtAsc(UUID bookingId);
}
