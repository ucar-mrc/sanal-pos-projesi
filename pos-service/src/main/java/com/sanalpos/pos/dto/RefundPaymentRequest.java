package com.sanalpos.pos.dto;

import jakarta.validation.constraints.NotNull;

public record RefundPaymentRequest(
        @NotNull(message = "Payment ID zorunlu")
        Long paymentId
) {
}