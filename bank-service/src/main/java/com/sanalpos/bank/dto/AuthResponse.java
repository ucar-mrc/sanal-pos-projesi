package com.sanalpos.bank.dto;

public record AuthResponse(
        String token,
        String email,
        Long customerId
) {}