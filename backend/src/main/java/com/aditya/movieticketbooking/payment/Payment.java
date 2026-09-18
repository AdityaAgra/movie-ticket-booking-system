package com.aditya.movieticketbooking.payment;

import com.aditya.movieticketbooking.booking.Booking;
import com.aditya.movieticketbooking.common.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Payment() { }
    private Payment(Booking booking, BigDecimal amount) {
        this.booking = booking;
        this.amount = amount;
        this.status = PaymentStatus.SUCCESS;
    }
    public static Payment success(Booking booking) { return new Payment(booking, booking.getTotalAmount()); }
    @PrePersist
    void initializeCreatedAt() { if (createdAt == null) { createdAt = Instant.now(); } }
    public UUID getId() { return id; }
    public Booking getBooking() { return booking; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
