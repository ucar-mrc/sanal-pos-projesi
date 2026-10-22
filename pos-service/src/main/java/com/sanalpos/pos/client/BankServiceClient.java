package com.sanalpos.pos.client;

import com.sanalpos.pos.dto.BankCreditSellerRequest;
import com.sanalpos.pos.dto.BankPaymentRequest;
import com.sanalpos.pos.dto.BankPaymentResponse;
import com.sanalpos.pos.dto.BankRefundRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "bank-service")
public interface BankServiceClient {

    @PostMapping("/api/v1/transactions/card-payment")
    BankPaymentResponse cardPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody BankPaymentRequest request
    );

    @PostMapping("/api/v1/transactions/credit-seller")
    BankPaymentResponse creditSeller(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody BankCreditSellerRequest request
    );
    @PostMapping("/api/v1/transactions/refund")
    BankPaymentResponse refund(@RequestBody BankRefundRequest request);
}