package com.sanalpos.pos.service;

import com.sanalpos.pos.client.BankServiceClient;
import com.sanalpos.pos.domain.CommissionRate;
import com.sanalpos.pos.domain.Payment;
import com.sanalpos.pos.domain.PaymentStatus;
import com.sanalpos.pos.dto.BankPaymentRequest;
import com.sanalpos.pos.dto.BankPaymentResponse;
import com.sanalpos.pos.dto.PaymentRequest;
import com.sanalpos.pos.dto.PaymentResponse;
import com.sanalpos.pos.exception.ResourceNotFoundException;
import com.sanalpos.pos.repository.CommissionRateRepository;
import com.sanalpos.pos.repository.PaymentRepository;
import com.sanalpos.pos.util.CardHashUtil;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final CommissionRateRepository commissionRateRepository;
    private final BankServiceClient bankServiceClient;

    public PaymentService(PaymentRepository paymentRepository,
                          CommissionRateRepository commissionRateRepository,
                          BankServiceClient bankServiceClient) {
        this.paymentRepository = paymentRepository;
        this.commissionRateRepository = commissionRateRepository;
        this.bankServiceClient = bankServiceClient;
    }

    /**
     * NOT: @Transactional YOK — bilinçli karar.
     * Payment kaydı PENDING olarak yazılır, Feign çağrısı yapılır, sonuç güncellenir.
     * Feign çağrısı uzun sürebilir; @Transactional olsaydı DB bağlantısı bekletilirdi.
     */
    public PaymentResponse processPayment(PaymentRequest request, String idempotencyKey) {

        // 1) Idempotency kontrolü
        String effectiveKey = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey
                : UUID.randomUUID().toString();

        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(effectiveKey);
        if (existing.isPresent()) {
            log.info("Idempotent istek algılandı, mevcut kayıt dönülüyor: key={}", effectiveKey);
            return toResponse(existing.get());
        }

        // 2) Komisyon oranını bul
        CommissionRate commissionRate = commissionRateRepository
                .findBySettlementDaysAndActiveTrue(request.settlementDays())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CommissionRate", "settlementDays", request.settlementDays()));

        // 3) Hesapla
        BigDecimal commissionAmount = request.amount().multiply(commissionRate.getRate());
        BigDecimal netAmount = request.amount().subtract(commissionAmount);

        // 4) PENDING kaydı oluştur ve kaydet (kendi transaction'ında commit olur)
        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setCustomerId(request.customerId());
        payment.setSellerId(request.sellerId());
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setSettlementDays(request.settlementDays());
        payment.setCommissionRate(commissionRate.getRate());
        payment.setCommissionAmount(commissionAmount);
        payment.setNetAmount(netAmount);
        payment.setCardLast4(CardHashUtil.last4(CardHashUtil.normalize(request.cardNumber())));
        payment.setIdempotencyKey(effectiveKey);
        payment.setDescription(request.description());

        Payment saved = paymentRepository.saveAndFlush(payment);
        log.info("Payment PENDING oluşturuldu: id={}, orderId={}", saved.getId(), saved.getOrderId());

        // 5) Bank-service'e Feign çağrısı
        try {
            BankPaymentRequest bankRequest = new BankPaymentRequest(
                    request.cardNumber(),
                    request.amount(),
                    request.description() != null ? request.description() : "Sipariş ödemesi"
            );

            BankPaymentResponse bankResponse = bankServiceClient.cardPayment(effectiveKey + "-bank", bankRequest);

            // 6) Başarılı → AUTH_OK'a güncelle
            saved.setStatus(PaymentStatus.AUTH_OK);
            saved.setBankTransactionRef(bankResponse.referenceId());
            Payment updated = paymentRepository.saveAndFlush(saved);
            log.info("Payment başarılı: id={}, bankRef={}", updated.getId(), bankResponse.referenceId());
            return toResponse(updated);

        } catch (FeignException e) {
            // 7) Feign hatası → FAILED, sonra yukarı fırlat (GlobalExceptionHandler düzgün dönsün)
            saved.setStatus(PaymentStatus.FAILED);
            paymentRepository.saveAndFlush(saved);
            log.warn("Payment başarısız: id={}, bank status={}", saved.getId(), e.status());
            throw e;
        }
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getSellerId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getSettlementDays(),
                payment.getCommissionRate(),
                payment.getCommissionAmount(),
                payment.getNetAmount(),
                payment.getCardLast4(),
                payment.getBankTransactionRef(),
                payment.getDescription(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}