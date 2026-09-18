package com.aditya.movieticketbooking.show;

import com.aditya.movieticketbooking.common.enums.ShowSeatStatus;
import com.aditya.movieticketbooking.theater.Seat;
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
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "show_seats")
public class ShowSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShowSeatStatus status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "held_by_user_id")
    private User heldByUser;
    @Column(name = "hold_expiry")
    private Instant holdExpiry;

    protected ShowSeat() { }
    private ShowSeat(Show show, Seat seat) {
        this.show = show;
        this.seat = seat;
        this.status = ShowSeatStatus.AVAILABLE;
    }
    public static ShowSeat available(Show show, Seat seat) { return new ShowSeat(show, seat); }
    public UUID getId() { return id; }
    public Show getShow() { return show; }
    public Seat getSeat() { return seat; }
    public ShowSeatStatus getStatus() { return status; }
    public User getHeldByUser() { return heldByUser; }
    public Instant getHoldExpiry() { return holdExpiry; }

    public void hold(User user, Instant expiry) {
        this.status = ShowSeatStatus.HELD;
        this.heldByUser = user;
        this.holdExpiry = expiry;
    }

    public void releaseHold() {
        this.status = ShowSeatStatus.AVAILABLE;
        this.heldByUser = null;
        this.holdExpiry = null;
    }

    public void markBooked() {
        this.status = ShowSeatStatus.BOOKED;
        this.heldByUser = null;
        this.holdExpiry = null;
    }
}
