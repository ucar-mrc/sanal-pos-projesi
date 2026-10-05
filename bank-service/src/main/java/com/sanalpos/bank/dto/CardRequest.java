package com.sanalpos.bank.dto;

import jakarta.validation.constraints.*;

public record CardRequest(

        @NotNull(message = "Customer ID zorunlu")
        Long customerId,

        @NotBlank(message = "Kart numarası zorunlu")
        @Pattern(regexp = "^[0-9 ]{15,19}$", message = "Kart numarası 15-19 karakter, rakam ve boşluk içermeli")
        String cardNumber,

        @NotBlank(message = "Kart sahibi adı zorunlu")
        @Size(max = 200, message = "Kart sahibi adı 200 karakteri geçemez")
        String cardHolderName,

        @NotNull(message = "Son kullanma ayı zorunlu")
        @Min(value = 1, message = "Ay 1-12 arasında olmalı")
        @Max(value = 12, message = "Ay 1-12 arasında olmalı")
        Integer expiryMonth,

        @NotNull(message = "Son kullanma yılı zorunlu")
        @Min(value = 2025, message = "Son kullanma yılı geçersiz")
        @Max(value = 2099, message = "Son kullanma yılı geçersiz")
        Integer expiryYear,

        @NotBlank(message = "CVV zorunlu")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV 3 veya 4 haneli olmalı")
        String cvv,

        @NotBlank(message = "Kart tipi zorunlu")
        @Pattern(regexp = "^(VISA|MASTERCARD|AMEX|TROY)$", message = "Geçersiz kart tipi")
        String cardType
) {
}