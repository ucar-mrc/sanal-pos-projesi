package com.sanalpos.pos.dto;

import com.sanalpos.pos.domain.Currency;
import com.sanalpos.pos.domain.SettlementStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record SettlementResponse(
        Long id,
        Long sellerId,
        BigDecimal amount,
        Currency currency,
        SettlementStatus status,
        String bankTransactionRef,
        Instant requestedAt,
        Instant completedAt,
        String failureReason
) {
}