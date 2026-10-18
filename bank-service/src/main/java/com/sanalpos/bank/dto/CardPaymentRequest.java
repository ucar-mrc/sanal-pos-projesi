package com.sanalpos.bank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CardPaymentRequest(

        @NotBlank(message = "Kart numarası zorunlu")
        @Pattern(regexp = "^[0-9 ]{15,19}$", message = "Kart numarası geçersiz format")
        String cardNumber,

        @NotNull(message = "Tutar zorunlu")
        @DecimalMin(value = "0.01", message = "Tutar 0'dan büyük olmalı")
        @Digits(integer = 15, fraction = 4, message = "Tutar formatı geçersiz")
        BigDecimal amount,

        @Size(max = 500, message = "Açıklama 500 karakteri geçemez")
        String description
) {
}