package com.sanalpos.bank.dto;

import java.time.Instant;

public record CardResponse(
        Long id,
        Long customerId,
        String maskedCardNumber,
        String cardHolderName,
        Integer expiryMonth,
        Integer expiryYear,
        String cardType,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}