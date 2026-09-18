package com.aditya.movieticketbooking.pricing;

import java.math.BigDecimal;

public record PriceQuote(BigDecimal subtotal, BigDecimal discountAmount, BigDecimal finalAmount) { }
