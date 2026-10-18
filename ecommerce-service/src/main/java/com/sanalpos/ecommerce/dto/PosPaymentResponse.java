package com.sanalpos.ecommerce.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PosPaymentResponse(
        Long id,
        Long orderId,
        String status,
        BigDecimal amount,
        BigDecimal commissionAmount,
        BigDecimal netAmount,
        String bankTransactionRef,
        Instant createdAt
) {
}