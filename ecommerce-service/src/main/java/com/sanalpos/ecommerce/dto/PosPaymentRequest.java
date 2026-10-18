package com.sanalpos.ecommerce.dto;

import com.sanalpos.ecommerce.domain.Currency;
import java.math.BigDecimal;

public record PosPaymentRequest(
        Long orderId,
        Long customerId,
        Long sellerId,
        String cardNumber,
        BigDecimal amount,
        Currency currency,
        Integer settlementDays,
        String description
) {
}