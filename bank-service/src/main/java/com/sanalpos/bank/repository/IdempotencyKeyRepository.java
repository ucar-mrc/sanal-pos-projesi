package com.sanalpos.bank.repository;

import com.sanalpos.bank.domain.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByKey(String key);

    boolean existsByKey(String key);

    List<IdempotencyKey> findByCreatedAtBefore(Instant threshold);
}