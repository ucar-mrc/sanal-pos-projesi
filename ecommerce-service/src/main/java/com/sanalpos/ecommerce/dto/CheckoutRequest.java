package com.sanalpos.ecommerce.dto;

import jakarta.validation.constraints.*;

public record CheckoutRequest(
        @NotNull(message = "Müşteri ID zorunlu")
        Long customerId,

        @NotBlank(message = "Kart numarası zorunlu")
        @Pattern(regexp = "^[0-9 ]{15,19}$", message = "Kart numarası geçersiz format")
        String cardNumber,

        @NotNull(message = "Vade günü zorunlu")
        @Min(value = 0, message = "Vade negatif olamaz")
        Integer settlementDays
) {
}