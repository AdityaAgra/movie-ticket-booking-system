package com.aditya.movieticketbooking.refund;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "refund_policies")
public class RefundPolicy {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "minimum_hours_before_show", nullable = false, unique = true)
    private int minimumHoursBeforeShow;
    @Column(name = "refund_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal refundPercentage;
    @Column(nullable = false)
    private boolean active;

    protected RefundPolicy() { }
    private RefundPolicy(int minimumHoursBeforeShow, BigDecimal refundPercentage, boolean active) {
        this.minimumHoursBeforeShow = minimumHoursBeforeShow;
        this.refundPercentage = refundPercentage;
        this.active = active;
    }
    public static RefundPolicy create(int minimumHoursBeforeShow, BigDecimal refundPercentage, boolean active) {
        return new RefundPolicy(minimumHoursBeforeShow, refundPercentage, active);
    }
    public UUID getId() { return id; }
    public int getMinimumHoursBeforeShow() { return minimumHoursBeforeShow; }
    public BigDecimal getRefundPercentage() { return refundPercentage; }
    public boolean isActive() { return active; }
}
