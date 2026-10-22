package com.sanalpos.pos.controller;

import com.sanalpos.pos.dto.PaymentRequest;
import com.sanalpos.pos.dto.PaymentResponse;
import com.sanalpos.pos.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> pay(
            @RequestHeader(value = IDEMPOTENCY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody PaymentRequest request) {

        PaymentResponse response = paymentService.processPayment(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refund(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.refund(id));
    }
}