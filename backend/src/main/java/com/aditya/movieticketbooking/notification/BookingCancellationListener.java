package com.aditya.movieticketbooking.notification;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BookingCancellationListener {
    private final NotificationService notificationService;

    public BookingCancellationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async("notificationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingCancelled(BookingCancellationEvent event) {
        notificationService.sendCancellationRefund(event);
    }
}
