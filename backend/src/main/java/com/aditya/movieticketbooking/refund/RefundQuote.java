package com.aditya.movieticketbooking.refund;

import java.math.BigDecimal;

public record RefundQuote(BigDecimal refundPercentage, BigDecimal refundAmount) { }
