package com.sanalpos.pos.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BankPaymentResponse(
        Long transactionId,
        String referenceId,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String cardLast4,
        String status,
        Instant processedAt
) {
}