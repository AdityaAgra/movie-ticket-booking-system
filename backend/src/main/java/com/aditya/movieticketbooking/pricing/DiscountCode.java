package com.aditya.movieticketbooking.pricing;

import com.aditya.movieticketbooking.common.enums.DiscountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "discount_codes")
public class DiscountCode {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;
    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;
    @Column(name = "valid_to", nullable = false)
    private Instant validTo;
    @Column(nullable = false)
    private boolean active;

    protected DiscountCode() { }
    private DiscountCode(String code, DiscountType discountType, BigDecimal value, Instant validFrom, Instant validTo, boolean active) {
        this.code = code;
        this.discountType = discountType;
        this.value = value;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.active = active;
    }
    public static DiscountCode create(String code, DiscountType discountType, BigDecimal value, Instant validFrom, Instant validTo, boolean active) {
        return new DiscountCode(code, discountType, value, validFrom, validTo, active);
    }
    public UUID getId() { return id; }
    public String getCode() { return code; }
    public DiscountType getDiscountType() { return discountType; }
    public BigDecimal getValue() { return value; }
    public Instant getValidFrom() { return validFrom; }
    public Instant getValidTo() { return validTo; }
    public boolean isActive() { return active; }
}
