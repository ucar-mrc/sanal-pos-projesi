package com.sanalpos.bank.dto;

import com.sanalpos.bank.domain.AccountType;
import com.sanalpos.bank.domain.Currency;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
        Long id,
        Long customerId,
        String iban,
        BigDecimal balance,
        Currency currency,
        AccountType accountType,
        Instant createdAt,
        Instant updatedAt
) {
}