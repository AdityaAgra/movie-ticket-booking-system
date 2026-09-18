package com.aditya.movieticketbooking.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);
    private final long simulatedDelayMillis;

    public NotificationService(@Value("${notification.simulated-delay-millis:0}") long simulatedDelayMillis) {
        this.simulatedDelayMillis = simulatedDelayMillis;
    }

    public void sendBookingConfirmation(BookingConfirmedEvent event) {
        pauseIfConfigured();
        LOGGER.info("Booking {} confirmed for {}.", event.bookingId(), event.customerEmail());
    }

    public void sendCancellationRefund(BookingCancellationEvent event) {
        pauseIfConfigured();
        LOGGER.info("Booking {} cancelled for {}; refund amount: {}.",
                event.bookingId(), event.customerEmail(), event.refundAmount());
    }

    private void pauseIfConfigured() {
        if (simulatedDelayMillis <= 0) {
            return;
        }
        try {
            Thread.sleep(simulatedDelayMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
