package com.sanalpos.bank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequest(

        @NotBlank(message = "TCKN boş olamaz")
        @Pattern(regexp = "^[0-9]{11}$", message = "TCKN 11 haneli rakam olmalı")
        String tckn,

        @NotBlank(message = "Ad boş olamaz")
        @Size(max = 100, message = "Ad 100 karakteri geçemez")
        String firstName,

        @NotBlank(message = "Soyad boş olamaz")
        @Size(max = 100, message = "Soyad 100 karakteri geçemez")
        String lastName,

        @NotBlank(message = "Email boş olamaz")
        @Email(message = "Geçersiz email formatı")
        String email,

        @Pattern(regexp = "^[0-9]{10,20}$", message = "Telefon 10-20 haneli rakam olmalı")
        String phone
) {
}