package com.aditya.movieticketbooking.booking;

import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HoldExpiryScheduler {
    private final HoldExpiryService holdExpiryService;

    public HoldExpiryScheduler(HoldExpiryService holdExpiryService) {
        this.holdExpiryService = holdExpiryService;
    }

    @Scheduled(cron = "0 * * * * *")
    public void releaseExpiredHolds() {
        holdExpiryService.releaseExpiredHolds(Instant.now());
    }
}
