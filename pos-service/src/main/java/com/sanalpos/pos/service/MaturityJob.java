package com.sanalpos.pos.service;

import com.sanalpos.pos.domain.Payment;
import com.sanalpos.pos.domain.SellerBalance;
import com.sanalpos.pos.repository.PaymentRepository;
import com.sanalpos.pos.repository.SellerBalanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;

@Component
public class MaturityJob {

    private static final Logger log = LoggerFactory.getLogger(MaturityJob.class);

    private final PaymentRepository paymentRepository;
    private final SellerBalanceRepository sellerBalanceRepository;
    private final TransactionTemplate transactionTemplate;

    public MaturityJob(PaymentRepository paymentRepository,
                       SellerBalanceRepository sellerBalanceRepository,
                       PlatformTransactionManager transactionManager) {
        this.paymentRepository = paymentRepository;
        this.sellerBalanceRepository = sellerBalanceRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void matureBalances() {
        Instant now = Instant.now();
        List<Payment> maturedPayments = paymentRepository.findMaturedPayments(now);

        if (maturedPayments.isEmpty()) {
            return;
        }

        log.info("Vadesi dolmuş {} ödeme bulundu, işleniyor...", maturedPayments.size());

        for (Payment p : maturedPayments) {
            try {
                // Her ödeme KENDİ transaction'ında işlenir
                transactionTemplate.executeWithoutResult(status -> {
                    SellerBalance balance = sellerBalanceRepository.findBySellerIdWithLock(p.getSellerId())
                            .orElseThrow(() -> new IllegalStateException(
                                    "SellerBalance bulunamadı: " + p.getSellerId()));

                    // KRİTİK KONTROL: Pending bakiye yeterli mi?
                    if (balance.getPendingBalance().compareTo(p.getNetAmount()) < 0) {
                        throw new IllegalStateException("Bekleyen bakiye yetersiz! SellerId: "
                                + p.getSellerId() + ", Bekleyen: " + balance.getPendingBalance()
                                + ", İşlem: " + p.getNetAmount());
                    }

                    balance.setPendingBalance(balance.getPendingBalance().subtract(p.getNetAmount()));
                    balance.setAvailableBalance(balance.getAvailableBalance().add(p.getNetAmount()));

                    p.setMaturedAt(now);

                    // saveAndFlush ile hemen DB'ye yaz (matured_at dolsun ki bir daha işlenmesin)
                    paymentRepository.saveAndFlush(p);
                    sellerBalanceRepository.saveAndFlush(balance);
                });

                log.info("Ödeme vadeye ulaştı: paymentId={}, sellerId={}, amount={}",
                        p.getId(), p.getSellerId(), p.getNetAmount());

            } catch (Exception e) {
                log.error("Vade işleme hatası: paymentId={}, error={}", p.getId(), e.getMessage());
                // Hata olsa bile döngü devam eder, bir sonraki ödemeye geçer
            }
        }
    }
}