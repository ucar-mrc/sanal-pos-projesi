package com.sanalpos.bank.controller;

import com.sanalpos.bank.dto.*;
import com.sanalpos.bank.service.IdempotencyService;
import com.sanalpos.bank.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sanalpos.bank.dto.CardPaymentRequest;
import com.sanalpos.bank.dto.CardPaymentResponse;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final TransactionService transactionService;
    private final IdempotencyService idempotencyService;

    public TransactionController(TransactionService transactionService,
                                 IdempotencyService idempotencyService) {
        this.transactionService = transactionService;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping("/debit")
    public ResponseEntity<TransactionResponse> debit(
            @RequestHeader(value = IDEMPOTENCY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody DebitRequest request) {

        return executeIdempotent(
                idempotencyKey,
                "/api/v1/transactions/debit",
                () -> transactionService.debit(request)
        );
    }

    @PostMapping("/credit")
    public ResponseEntity<TransactionResponse> credit(
            @RequestHeader(value = IDEMPOTENCY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody CreditRequest request) {

        return executeIdempotent(
                idempotencyKey,
                "/api/v1/transactions/credit",
                () -> transactionService.credit(request)
        );
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @RequestHeader(value = IDEMPOTENCY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {

        return executeIdempotent(
                idempotencyKey,
                "/api/v1/transactions/transfer",
                () -> transactionService.transfer(request)
        );
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> getAccountTransactions(
            @PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.getAccountTransactions(accountId));
    }

    @PostMapping("/card-payment")
    public ResponseEntity<CardPaymentResponse> cardPayment(
            @RequestHeader(value = IDEMPOTENCY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody CardPaymentRequest request) {

        // Idempotency kontrolü
        if (hasKey(idempotencyKey)) {
            Optional<IdempotencyService.CachedResponse<CardPaymentResponse>> cached =
                    idempotencyService.findCached(idempotencyKey, CardPaymentResponse.class);
            if (cached.isPresent()) {
                return ResponseEntity.status(cached.get().status()).body(cached.get().body());
            }
        }

        CardPaymentResponse response = transactionService.cardPayment(request);

        if (hasKey(idempotencyKey)) {
            idempotencyService.save(idempotencyKey, "/api/v1/transactions/card-payment",
                    HttpStatus.CREATED.value(), response);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }@PostMapping("/credit-seller")
    public ResponseEntity<TransactionResponse> creditSeller(
            @RequestHeader(value = IDEMPOTENCY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody CreditSellerRequest request) {

        if (hasKey(idempotencyKey)) {
            Optional<IdempotencyService.CachedResponse<TransactionResponse>> cached =
                    idempotencyService.findCached(idempotencyKey, TransactionResponse.class);
            if (cached.isPresent()) {
                return ResponseEntity.status(cached.get().status()).body(cached.get().body());
            }
        }

        TransactionResponse response = transactionService.creditSeller(request);

        if (hasKey(idempotencyKey)) {
            idempotencyService.save(idempotencyKey, "/api/v1/transactions/credit-seller",
                    HttpStatus.CREATED.value(), response);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/refund")
    public ResponseEntity<TransactionResponse> refund(@Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(transactionService.refund(request));
    }

    // =====================================================
    // Idempotency wrapper
    // =====================================================
    private ResponseEntity<TransactionResponse> executeIdempotent(
            String idempotencyKey,
            String endpoint,
            Supplier<TransactionResponse> action) {

        // 1. Key varsa cache'de ara
        if (hasKey(idempotencyKey)) {
            Optional<IdempotencyService.CachedResponse<TransactionResponse>> cached =
                    idempotencyService.findCached(idempotencyKey, TransactionResponse.class);

            if (cached.isPresent()) {
                return ResponseEntity
                        .status(cached.get().status())
                        .body(cached.get().body());
            }
        }

        // 2. Cache miss → işi yap
        TransactionResponse response = action.get();

        // 3. Başarılıysa cache'le
        if (hasKey(idempotencyKey)) {
            idempotencyService.save(idempotencyKey, endpoint, HttpStatus.CREATED.value(), response);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private boolean hasKey(String key) {
        return key != null && !key.isBlank();
    }
}