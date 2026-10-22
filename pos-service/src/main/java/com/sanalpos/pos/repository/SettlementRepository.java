package com.sanalpos.pos.repository;

import com.sanalpos.pos.domain.Settlement;
import com.sanalpos.pos.domain.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findBySellerIdOrderByRequestedAtDesc(Long sellerId);

    List<Settlement> findByStatus(SettlementStatus status);
}