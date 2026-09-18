package com.aditya.movieticketbooking.notification;

import java.util.UUID;

public record BookingConfirmedEvent(UUID bookingId, String customerEmail) { }
