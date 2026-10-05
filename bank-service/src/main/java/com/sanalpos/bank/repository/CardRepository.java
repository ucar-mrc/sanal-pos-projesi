package com.sanalpos.bank.repository;

import com.sanalpos.bank.domain.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByCardNumberHash(String cardNumberHash);

    List<Card> findByCustomerId(Long customerId);

    boolean existsByCardNumberHash(String cardNumberHash);
}