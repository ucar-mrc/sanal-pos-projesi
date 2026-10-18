package com.sanalpos.pos.dto;

import com.sanalpos.pos.domain.Currency;
import com.sanalpos.pos.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        Long orderId,
        Long customerId,
        Long sellerId,
        BigDecimal amount,
        Currency currency,
        PaymentStatus status,
        Integer settlementDays,
        BigDecimal commissionRate,
        BigDecimal commissionAmount,
        BigDecimal netAmount,
        String cardLast4,
        String bankTransactionRef,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}