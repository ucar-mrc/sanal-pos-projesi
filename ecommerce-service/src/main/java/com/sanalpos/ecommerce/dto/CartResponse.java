package com.sanalpos.ecommerce.dto;

import com.sanalpos.ecommerce.domain.Currency;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,
        Long customerId,
        List<CartItemResponse> items,
        BigDecimal totalAmount,
        Currency currency
) {
    public record CartItemResponse(
            Long id,
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }
}