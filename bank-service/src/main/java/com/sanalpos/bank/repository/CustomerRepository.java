package com.sanalpos.bank.repository;

import com.sanalpos.bank.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByTckn(String tckn);

    Optional<Customer> findByEmail(String email);

    boolean existsByTckn(String tckn);

    boolean existsByEmail(String email);
}