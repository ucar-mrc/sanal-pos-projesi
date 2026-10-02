package com.sanalpos.bank.repository;

import com.sanalpos.bank.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByIban(String iban);

    List<Account> findByCustomerId(Long customerId);

    boolean existsByIban(String iban);
}