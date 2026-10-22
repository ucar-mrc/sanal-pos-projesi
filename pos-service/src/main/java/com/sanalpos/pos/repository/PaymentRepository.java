package com.sanalpos.pos.repository;

import com.sanalpos.pos.domain.Payment;
import com.sanalpos.pos.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Payment> findBySellerIdAndStatus(Long sellerId, PaymentStatus status);

    List<Payment> findByStatus(PaymentStatus status);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query(value = """
    SELECT * FROM payments p 
    WHERE p.status = 'AUTH_OK'
    AND p.matured_at IS NULL
    AND (p.created_at + (p.settlement_days * INTERVAL '1 day')) <= :now
    """, nativeQuery = true)
    List<Payment> findMaturedPayments(@Param("now") Instant now);
}