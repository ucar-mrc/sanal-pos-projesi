package com.sanalpos.bank.dto;

import com.sanalpos.bank.domain.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreditSellerRequest(
        @NotNull(message = "Satıcı (Müşteri) ID zorunlu")
        Long customerId,

        @NotNull(message = "Tutar zorunlu")
        @DecimalMin(value = "0.01", message = "Tutar 0'dan büyük olmalı")
        @Digits(integer = 15, fraction = 4)
        BigDecimal amount,

        @NotNull(message = "Para birimi zorunlu")
        Currency currency,

        String description
) {
}