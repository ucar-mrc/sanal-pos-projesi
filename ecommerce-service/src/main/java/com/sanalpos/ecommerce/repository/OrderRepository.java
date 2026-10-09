package com.sanalpos.ecommerce.repository;

import com.sanalpos.ecommerce.domain.Order;
import com.sanalpos.ecommerce.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Order> findByStatus(OrderStatus status);

    boolean existsByCustomerIdAndStatus(Long customerId, OrderStatus status);
}