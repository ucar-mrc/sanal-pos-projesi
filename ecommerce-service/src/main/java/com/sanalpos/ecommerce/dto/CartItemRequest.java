package com.sanalpos.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequest(
        @NotNull Long customerId,
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity
) {
}