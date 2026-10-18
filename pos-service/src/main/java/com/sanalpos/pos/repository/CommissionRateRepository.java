package com.sanalpos.pos.repository;

import com.sanalpos.pos.domain.CommissionRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommissionRateRepository extends JpaRepository<CommissionRate, Long> {

    Optional<CommissionRate> findBySettlementDaysAndActiveTrue(Integer settlementDays);

    List<CommissionRate> findByActiveTrue();
}