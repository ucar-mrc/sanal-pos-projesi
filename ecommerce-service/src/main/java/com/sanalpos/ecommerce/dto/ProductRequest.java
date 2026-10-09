package com.sanalpos.ecommerce.dto;

import com.sanalpos.ecommerce.domain.Currency;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(

        @NotNull(message = "Seller ID zorunlu")
        Long sellerId,

        @NotBlank(message = "Ürün adı zorunlu")
        @Size(max = 200, message = "Ürün adı 200 karakteri geçemez")
        String name,

        @Size(max = 2000, message = "Açıklama 2000 karakteri geçemez")
        String description,

        @NotNull(message = "Fiyat zorunlu")
        @DecimalMin(value = "0.01", message = "Fiyat 0'dan büyük olmalı")
        @Digits(integer = 15, fraction = 4, message = "Fiyat formatı geçersiz")
        BigDecimal price,

        @NotNull(message = "Para birimi zorunlu")
        Currency currency,

        @NotNull(message = "Stok zorunlu")
        @Min(value = 0, message = "Stok negatif olamaz")
        Integer stock
) {
}