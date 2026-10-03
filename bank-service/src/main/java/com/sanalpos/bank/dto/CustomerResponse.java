package com.sanalpos.bank.dto;

import java.time.Instant;

public record CustomerResponse(
        Long id,
        String tckn,
        String firstName,
        String lastName,
        String email,
        String phone,
        Instant createdAt,
        Instant updatedAt
) {
}