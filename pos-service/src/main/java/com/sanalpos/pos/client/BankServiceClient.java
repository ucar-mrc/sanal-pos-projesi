package com.sanalpos.pos.client;

import com.sanalpos.pos.dto.BankPaymentRequest;
import com.sanalpos.pos.dto.BankPaymentResponse;
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
}