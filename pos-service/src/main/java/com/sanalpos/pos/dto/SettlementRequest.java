package com.sanalpos.pos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SettlementRequest(
        @NotNull(message = "Satıcı ID zorunlu")
        Long sellerId,

        @NotNull(message = "Tutar zorunlu")
        @DecimalMin(value = "0.01", message = "Tutar 0'dan büyük olmalı")
        BigDecimal amount
) {
}