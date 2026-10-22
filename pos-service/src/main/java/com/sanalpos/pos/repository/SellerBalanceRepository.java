package com.sanalpos.pos.repository;

import com.sanalpos.pos.domain.SellerBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SellerBalanceRepository extends JpaRepository<SellerBalance, Long> {

    Optional<SellerBalance> findBySellerId(Long sellerId);

    boolean existsBySellerId(Long sellerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sb FROM SellerBalance sb WHERE sb.sellerId = :sellerId")
    Optional<SellerBalance> findBySellerIdWithLock(@Param("sellerId") Long sellerId);
}