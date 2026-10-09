package com.sanalpos.ecommerce.repository;

import com.sanalpos.ecommerce.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findBySellerId(Long sellerId);

    List<Product> findByActiveTrue();

    List<Product> findBySellerIdAndActiveTrue(Long sellerId);

    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);
}