package com.sanalpos.bank.dto;

import jakarta.validation.constraints.NotBlank;

public record RefundRequest(
        @NotBlank(message = "İade edilecek işlem referansı zorunlu")
        String bankTransactionRef
) {
}