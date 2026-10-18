package com.sanalpos.ecommerce.client;

import com.sanalpos.ecommerce.dto.PosPaymentRequest;
import com.sanalpos.ecommerce.dto.PosPaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "pos-service")
public interface PosServiceClient {

    @PostMapping("/api/v1/payments")
    PosPaymentResponse pay(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody PosPaymentRequest request
    );
}