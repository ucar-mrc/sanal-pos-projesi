package com.sanalpos.ecommerce.repository;

import com.sanalpos.ecommerce.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findBySellerId(Long sellerId);
}