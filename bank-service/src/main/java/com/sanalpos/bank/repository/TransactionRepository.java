package com.sanalpos.bank.repository;

import com.sanalpos.bank.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    List<Transaction> findByReferenceId(String referenceId);

    Optional<Transaction> findByIdAndAccountId(Long id, Long accountId);
}