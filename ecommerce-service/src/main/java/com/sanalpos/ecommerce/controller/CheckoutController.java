package com.sanalpos.ecommerce.controller;

import com.sanalpos.ecommerce.dto.CheckoutRequest;
import com.sanalpos.ecommerce.dto.CheckoutResponse;
import com.sanalpos.ecommerce.service.CheckoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping
    public ResponseEntity<CheckoutResponse> checkout(
            @RequestHeader(value = IDEMPOTENCY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody CheckoutRequest request) {

        CheckoutResponse response = checkoutService.checkout(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}