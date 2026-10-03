package com.sanalpos.bank.dto;

import com.sanalpos.bank.domain.AccountType;
import com.sanalpos.bank.domain.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AccountRequest(

        @NotNull(message = "Customer ID zorunlu")
        Long customerId,

        @NotBlank(message = "IBAN boş olamaz")
        @Pattern(regexp = "^TR[0-9]{24}$", message = "IBAN 'TR' ile başlamalı ve 26 karakter olmalı")
        String iban,

        @NotNull(message = "Para birimi zorunlu")
        Currency currency,

        @NotNull(message = "Hesap tipi zorunlu")
        AccountType accountType
) {
}