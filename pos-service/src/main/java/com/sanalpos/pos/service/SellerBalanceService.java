package com.sanalpos.pos.service;

import com.sanalpos.pos.domain.Currency;
import com.sanalpos.pos.domain.SellerBalance;
import com.sanalpos.pos.exception.ResourceNotFoundException;
import com.sanalpos.pos.repository.SellerBalanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class SellerBalanceService {

    private static final Logger log = LoggerFactory.getLogger(SellerBalanceService.class);

    private final SellerBalanceRepository sellerBalanceRepository;

    public SellerBalanceService(SellerBalanceRepository sellerBalanceRepository) {
        this.sellerBalanceRepository = sellerBalanceRepository;
    }

    /**
     * Ödeme başarılı olduğunda satıcının bekleyen bakiyesini artırır.
     * Cüzdan yoksa oluşturur (lazy create).
     */
    @Transactional
    public void addPendingBalance(Long sellerId, BigDecimal amount, Currency currency) {
        SellerBalance balance = sellerBalanceRepository.findBySellerIdWithLock(sellerId)
                .orElseGet(() -> {
                    log.info("Satıcı için yeni cüzdan oluşturuluyor: sellerId={}", sellerId);
                    SellerBalance newBalance = new SellerBalance();
                    newBalance.setSellerId(sellerId);
                    newBalance.setCurrency(currency);
                    newBalance.setPendingBalance(BigDecimal.ZERO);
                    newBalance.setAvailableBalance(BigDecimal.ZERO);
                    newBalance.setTotalWithdrawn(BigDecimal.ZERO);
                    return newBalance;
                });

        if (balance.getCurrency() != currency) {
            throw new IllegalArgumentException("Para birimi uyuşmazlığı! Cüzdan: "
                    + balance.getCurrency() + ", İşlem: " + currency);
        }

        balance.setPendingBalance(balance.getPendingBalance().add(amount));
        sellerBalanceRepository.saveAndFlush(balance);

        log.info("Satıcı bekleyen bakiyesi güncellendi: sellerId={}, +{}, yeni pending={}",
                sellerId, amount, balance.getPendingBalance());
    }
}