package com.sanalpos.bank.dto;

import com.sanalpos.bank.domain.Currency;
import com.sanalpos.bank.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        Long accountId,
        TransactionType type,
        BigDecimal amount,
        Currency currency,
        BigDecimal balanceAfter,
        String referenceId,
        String description,
        Instant createdAt
) {
}