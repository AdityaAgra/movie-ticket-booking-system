package com.aditya.movieticketbooking.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BookingConfirmationListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookingConfirmationListener.class);

    @Async("notificationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        LOGGER.info("Booking {} confirmed for {}.", event.bookingId(), event.customerEmail());
    }
}
