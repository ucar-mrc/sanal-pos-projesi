package com.sanalpos.pos.dto;

import java.math.BigDecimal;

public record BankPaymentRequest(
        String cardNumber,
        BigDecimal amount,
        String description
) {
}