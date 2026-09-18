package com.aditya.movieticketbooking.theater;

import com.aditya.movieticketbooking.common.enums.SeatType;
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
import java.util.UUID;

@Entity
@Table(name = "seats")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "row_label", nullable = false, length = 10)
    private String rowLabel;
    @Column(name = "seat_number", nullable = false)
    private int seatNumber;
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false, length = 20)
    private SeatType seatType;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auditorium_id", nullable = false)
    private Auditorium auditorium;

    protected Seat() { }
    private Seat(String rowLabel, int seatNumber, SeatType seatType, Auditorium auditorium) {
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.auditorium = auditorium;
    }
    public static Seat create(String rowLabel, int seatNumber, SeatType seatType, Auditorium auditorium) {
        return new Seat(rowLabel, seatNumber, seatType, auditorium);
    }
    public UUID getId() { return id; }
    public String getRowLabel() { return rowLabel; }
    public int getSeatNumber() { return seatNumber; }
    public SeatType getSeatType() { return seatType; }
    public Auditorium getAuditorium() { return auditorium; }
}
