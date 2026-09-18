package com.aditya.movieticketbooking.booking;

import com.aditya.movieticketbooking.show.ShowSeat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "booking_seats")
public class BookingSeat {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_seat_id", nullable = false)
    private ShowSeat showSeat;
    @Column(name = "price_at_booking", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtBooking;

    protected BookingSeat() { }
    private BookingSeat(Booking booking, ShowSeat showSeat, BigDecimal priceAtBooking) {
        this.booking = booking;
        this.showSeat = showSeat;
        this.priceAtBooking = priceAtBooking;
    }
    public static BookingSeat create(Booking booking, ShowSeat showSeat, BigDecimal priceAtBooking) {
        return new BookingSeat(booking, showSeat, priceAtBooking);
    }
    public UUID getId() { return id; }
    public Booking getBooking() { return booking; }
    public ShowSeat getShowSeat() { return showSeat; }
    public BigDecimal getPriceAtBooking() { return priceAtBooking; }
}
