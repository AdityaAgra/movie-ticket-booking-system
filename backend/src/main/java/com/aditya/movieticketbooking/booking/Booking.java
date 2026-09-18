package com.aditya.movieticketbooking.booking;

import com.aditya.movieticketbooking.common.enums.BookingStatus;
import com.aditya.movieticketbooking.show.Show;
import com.aditya.movieticketbooking.user.User;
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
@Table(name = "bookings")
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Booking() { }
    private Booking(User user, Show show, BigDecimal totalAmount) {
        this.user = user;
        this.show = show;
        this.status = BookingStatus.PENDING;
        this.totalAmount = totalAmount;
    }
    public static Booking pending(User user, Show show, BigDecimal totalAmount) {
        return new Booking(user, show, totalAmount);
    }
    @PrePersist
    void initializeCreatedAt() { if (createdAt == null) { createdAt = Instant.now(); } }
    public UUID getId() { return id; }
    public User getUser() { return user; }
    public Show getShow() { return show; }
    public BookingStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Instant getCreatedAt() { return createdAt; }

    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
    }

    public void expire() {
        this.status = BookingStatus.EXPIRED;
    }
}
