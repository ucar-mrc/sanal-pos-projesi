package com.sanalpos.pos.service;

import com.sanalpos.pos.client.BankServiceClient;
import com.sanalpos.pos.domain.SellerBalance;
import com.sanalpos.pos.domain.Settlement;
import com.sanalpos.pos.domain.SettlementStatus;
import com.sanalpos.pos.dto.*;
import com.sanalpos.pos.exception.ResourceNotFoundException;
import com.sanalpos.pos.repository.SellerBalanceRepository;
import com.sanalpos.pos.repository.SettlementRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.UUID;

@Service
public class SettlementService {

    private static final Logger log = LoggerFactory.getLogger(SettlementService.class);

    private final SellerBalanceRepository sellerBalanceRepository;
    private final SettlementRepository settlementRepository;
    private final BankServiceClient bankServiceClient;
    private final TransactionTemplate transactionTemplate;

    public SettlementService(SellerBalanceRepository sellerBalanceRepository,
                             SettlementRepository settlementRepository,
                             BankServiceClient bankServiceClient,
                             PlatformTransactionManager transactionManager) { // Spring Boot bunu otomatik verir
        this.sellerBalanceRepository = sellerBalanceRepository;
        this.settlementRepository = settlementRepository;
        this.bankServiceClient = bankServiceClient;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public SettlementResponse requestSettlement(SettlementRequest request, String idempotencyKey) {
        String effectiveKey = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey : UUID.randomUUID().toString();

        // 1. DB Transaction: Bakiye düş, PENDING settlement oluştur
        Settlement savedSettlement = transactionTemplate.execute(status -> {
            SellerBalance balance = sellerBalanceRepository.findBySellerIdWithLock(request.sellerId())
                    .orElseThrow(() -> new ResourceNotFoundException("SellerBalance", "sellerId", request.sellerId()));

            if (balance.getAvailableBalance().compareTo(request.amount()) < 0) {
                throw new IllegalArgumentException(
                        "Yetersiz bakiye. Mevcut: " + balance.getAvailableBalance()
                                + ", istenen: " + request.amount());
            }

            balance.setAvailableBalance(balance.getAvailableBalance().subtract(request.amount()));
            balance.setTotalWithdrawn(balance.getTotalWithdrawn().add(request.amount()));
            sellerBalanceRepository.saveAndFlush(balance);

            Settlement settlement = new Settlement();
            settlement.setSellerId(request.sellerId());
            settlement.setAmount(request.amount());
            settlement.setCurrency(balance.getCurrency());
            settlement.setStatus(SettlementStatus.PENDING);
            return settlementRepository.saveAndFlush(settlement);
        });

        // 2. Banka çağrısı (Transaction dışında - DB bağlantısı serbest kalır)
        try {
            BankCreditSellerRequest bankReq = new BankCreditSellerRequest(
                    request.sellerId(),
                    request.amount(),
                    savedSettlement.getCurrency(),
                    "Settlement #" + savedSettlement.getId()
            );

            BankPaymentResponse bankRes = bankServiceClient.creditSeller(effectiveKey + "-settle", bankReq);

            // 3. DB Transaction: Başarılı, COMPLETED yap
            return transactionTemplate.execute(status -> {
                savedSettlement.setStatus(SettlementStatus.COMPLETED);
                savedSettlement.setBankTransactionRef(bankRes.referenceId());
                savedSettlement.setCompletedAt(Instant.now());
                Settlement completed = settlementRepository.saveAndFlush(savedSettlement);
                log.info("Settlement başarılı: id={}, ref={}", completed.getId(), bankRes.referenceId());
                return toResponse(completed);
            });

        } catch (FeignException e) {
            // 4. DB Transaction: Hata, bakiyeyi geri yükle (Compensation) ve FAILED yap
            transactionTemplate.execute(status -> {
                SellerBalance balance = sellerBalanceRepository.findBySellerIdWithLock(request.sellerId()).orElseThrow();
                balance.setAvailableBalance(balance.getAvailableBalance().add(request.amount()));
                balance.setTotalWithdrawn(balance.getTotalWithdrawn().subtract(request.amount()));
                sellerBalanceRepository.saveAndFlush(balance);

                savedSettlement.setStatus(SettlementStatus.FAILED);
                savedSettlement.setFailureReason("Banka hatası: " + e.status());
                settlementRepository.saveAndFlush(savedSettlement);
                return null;
            });

            log.warn("Settlement başarısız: id={}, hata={}", savedSettlement.getId(), e.getMessage());
            throw e;
        }
    }

    private SettlementResponse toResponse(Settlement s) {
        return new SettlementResponse(
                s.getId(), s.getSellerId(), s.getAmount(), s.getCurrency(),
                s.getStatus(), s.getBankTransactionRef(), s.getRequestedAt(),
                s.getCompletedAt(), s.getFailureReason()
        );
    }
}