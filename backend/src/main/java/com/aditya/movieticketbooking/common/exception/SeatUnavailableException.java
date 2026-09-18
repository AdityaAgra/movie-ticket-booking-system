package com.aditya.movieticketbooking.common.exception;

public class SeatUnavailableException extends RuntimeException {
    public SeatUnavailableException() {
        super("One or more selected seats are unavailable.");
    }
}
