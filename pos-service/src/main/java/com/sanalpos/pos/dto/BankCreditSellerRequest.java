package com.sanalpos.pos.dto;

import com.sanalpos.pos.domain.Currency;
import java.math.BigDecimal;

public record BankCreditSellerRequest(
        Long customerId,
        BigDecimal amount,
        Currency currency,
        String description
) {
}