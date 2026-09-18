package com.aditya.movieticketbooking.notification;

import java.math.BigDecimal;
import java.util.UUID;

public record BookingCancellationEvent(UUID bookingId, String customerEmail, BigDecimal refundAmount) { }
