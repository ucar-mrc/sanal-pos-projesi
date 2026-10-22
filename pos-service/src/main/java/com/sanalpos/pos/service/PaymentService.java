package com.sanalpos.pos.service;

import com.sanalpos.pos.client.BankServiceClient;
import com.sanalpos.pos.domain.CommissionRate;
import com.sanalpos.pos.domain.Payment;
import com.sanalpos.pos.domain.PaymentStatus;
import com.sanalpos.pos.domain.SellerBalance;
import com.sanalpos.pos.dto.*;
import com.sanalpos.pos.exception.ResourceNotFoundException;
import com.sanalpos.pos.repository.CommissionRateRepository;
import com.sanalpos.pos.repository.PaymentRepository;
import com.sanalpos.pos.repository.SellerBalanceRepository;
import com.sanalpos.pos.util.CardHashUtil;
import feign.FeignException;
import jakarta.transaction.Transactional;
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
    private final SellerBalanceService sellerBalanceService;
    private final SellerBalanceRepository sellerBalanceRepository;// YENİ

    public PaymentService(PaymentRepository paymentRepository,
                          CommissionRateRepository commissionRateRepository,
                          BankServiceClient bankServiceClient,
                          SellerBalanceService sellerBalanceService,
                          SellerBalanceRepository sellerBalanceRepository) {
        this.paymentRepository = paymentRepository;
        this.commissionRateRepository = commissionRateRepository;
        this.bankServiceClient = bankServiceClient;
        this.sellerBalanceService = sellerBalanceService;
        this.sellerBalanceRepository = sellerBalanceRepository;
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

// 6.1) Satıcının bekleyen bakiyesini artır
            sellerBalanceService.addPendingBalance(
                    updated.getSellerId(),
                    updated.getNetAmount(),
                    updated.getCurrency()
            );

            return toResponse(updated);

        } catch (FeignException e) {
            // 7) Feign hatası → FAILED, sonra yukarı fırlat (GlobalExceptionHandler düzgün dönsün)
            saved.setStatus(PaymentStatus.FAILED);
            paymentRepository.saveAndFlush(saved);
            log.warn("Payment başarısız: id={}, bank status={}", saved.getId(), e.status());
            throw e;
        }
    }

    @Transactional
    public PaymentResponse refund(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (payment.getStatus() != PaymentStatus.AUTH_OK) {
            throw new IllegalArgumentException("Sadece AUTH_OK ödemeler iade edilebilir. Mevcut: " + payment.getStatus());
        }

        // 1. Banka Servisi'ni çağır (Transaction dışında olsaydı daha iyiydi ama kısa tutuyoruz)
        BankRefundRequest req = new BankRefundRequest(payment.getBankTransactionRef());
        try {
            bankServiceClient.refund(req);
        } catch (FeignException e) {
            throw new IllegalStateException("Banka iadesi başarısız: " + e.contentUTF8());
        }

        // 2. Satıcı bakiyesini düş
        SellerBalance balance = sellerBalanceRepository.findBySellerIdWithLock(payment.getSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("SellerBalance", "sellerId", payment.getSellerId()));

        // Kural: Eğer para available_balance'a geçmişse oradan, geçmemişse pending'den düş
        if (payment.getMaturedAt() != null) {
            // Para çekilebilir durumdaydı
            if (balance.getAvailableBalance().compareTo(payment.getNetAmount()) < 0) {
                throw new IllegalStateException("Satıcının iade için yeterli available bakiyesi yok!");
            }
            balance.setAvailableBalance(balance.getAvailableBalance().subtract(payment.getNetAmount()));
        } else {
            // Para hala pending'de
            if (balance.getPendingBalance().compareTo(payment.getNetAmount()) < 0) {
                throw new IllegalStateException("Satıcının iade için yeterli pending bakiyesi yok!");
            }
            balance.setPendingBalance(balance.getPendingBalance().subtract(payment.getNetAmount()));
        }

        sellerBalanceRepository.saveAndFlush(balance);

        // 3. Ödeme durumunu güncelle
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.saveAndFlush(payment);

        log.info("Ödeme iade edildi: id={}, sellerId={}", payment.getId(), payment.getSellerId());

        return toResponse(payment);
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