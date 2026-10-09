package com.sanalpos.ecommerce.dto;

import com.sanalpos.ecommerce.domain.Currency;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        Long sellerId,
        String name,
        String description,
        BigDecimal price,
        Currency currency,
        Integer stock,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}