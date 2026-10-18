package com.sanalpos.bank.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CardPaymentResponse(
        Long transactionId,
        String referenceId,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String cardLast4,
        String status,
        Instant processedAt
) {
}