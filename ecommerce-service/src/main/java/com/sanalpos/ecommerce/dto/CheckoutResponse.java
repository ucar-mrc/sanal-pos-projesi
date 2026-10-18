package com.sanalpos.ecommerce.dto;

import com.sanalpos.ecommerce.domain.Currency;
import com.sanalpos.ecommerce.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record CheckoutResponse(
        Long orderId,
        OrderStatus status,
        BigDecimal totalAmount,
        Currency currency,
        String bankTransactionRef,
        Instant createdAt
) {
}