package com.sanalpos.pos.client;

import com.sanalpos.pos.dto.BankCreditSellerRequest;
import com.sanalpos.pos.dto.BankPaymentRequest;
import com.sanalpos.pos.dto.BankPaymentResponse;
import com.sanalpos.pos.dto.BankRefundRequest;
import com.sanalpos.pos.exception.BankServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BankServiceFallback implements BankServiceClient {

    private static final Logger log = LoggerFactory.getLogger(BankServiceFallback.class);

    @Override
    public BankPaymentResponse cardPayment(String idempotencyKey, BankPaymentRequest request) {
        log.error("CircuitBreaker: Banka servisi (cardPayment) ulaşılamıyor! Key: {}", idempotencyKey);
        throw new BankServiceUnavailableException("Banka sistemi şu anda yanıt vermiyor. Lütfen daha sonra tekrar deneyin.");
    }

    @Override
    public BankPaymentResponse creditSeller(String idempotencyKey, BankCreditSellerRequest request) {
        log.error("CircuitBreaker: Banka servisi (creditSeller) ulaşılamıyor! Key: {}", idempotencyKey);
        throw new BankServiceUnavailableException("Banka sistemi şu anda yanıt vermiyor.");
    }

    @Override
    public BankPaymentResponse refund(BankRefundRequest request) {
        log.error("CircuitBreaker: Banka servisi (refund) ulaşılamıyor!");
        throw new BankServiceUnavailableException("Banka sistemi şu anda yanıt vermiyor.");
    }
}