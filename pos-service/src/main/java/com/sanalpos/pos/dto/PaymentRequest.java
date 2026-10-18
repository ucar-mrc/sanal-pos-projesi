package com.sanalpos.pos.dto;

import com.sanalpos.pos.domain.Currency;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PaymentRequest(

        @NotNull(message = "Sipariş ID zorunlu")
        Long orderId,

        @NotNull(message = "Müşteri ID zorunlu")
        Long customerId,

        @NotNull(message = "Satıcı ID zorunlu")
        Long sellerId,

        @NotBlank(message = "Kart numarası zorunlu")
        @Pattern(regexp = "^[0-9 ]{15,19}$", message = "Kart numarası geçersiz format")
        String cardNumber,

        @NotNull(message = "Tutar zorunlu")
        @DecimalMin(value = "0.01", message = "Tutar 0'dan büyük olmalı")
        @Digits(integer = 15, fraction = 4, message = "Tutar formatı geçersiz")
        BigDecimal amount,

        @NotNull(message = "Para birimi zorunlu")
        Currency currency,

        @NotNull(message = "Vade günü zorunlu")
        @Min(value = 0, message = "Vade negatif olamaz")
        Integer settlementDays,

        @Size(max = 500, message = "Açıklama 500 karakteri geçemez")
        String description
) {
}