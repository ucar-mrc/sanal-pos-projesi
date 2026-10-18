package com.sanalpos.ecommerce.service;

import com.sanalpos.ecommerce.client.PosServiceClient;
import com.sanalpos.ecommerce.domain.*;
import com.sanalpos.ecommerce.dto.*;
import com.sanalpos.ecommerce.exception.ResourceNotFoundException;
import com.sanalpos.ecommerce.repository.CartRepository;
import com.sanalpos.ecommerce.repository.OrderRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final PosServiceClient posServiceClient;

    public CheckoutService(CartRepository cartRepository,
                           OrderRepository orderRepository,
                           PosServiceClient posServiceClient) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.posServiceClient = posServiceClient;
    }

    public CheckoutResponse checkout(CheckoutRequest request, String idempotencyKey) {

        Cart cart = cartRepository.findByCustomerId(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "customerId", request.customerId()));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Sepet boş");
        }

        // Tek satıcı kontrolü (basit simülasyon)
        Set<Long> sellerIds = cart.getItems().stream()
                .map(item -> item.getProduct().getSellerId())
                .collect(Collectors.toSet());

        if (sellerIds.size() > 1) {
            throw new IllegalArgumentException(
                    "Sepetinizde birden fazla satıcıdan ürün var. Lütfen her satıcı için ayrı sipariş oluşturun.");
        }
        Long sellerId = sellerIds.iterator().next();

        // Sipariş oluştur (PENDING)
        Order order = new Order();
        order.setCustomerId(request.customerId());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        Currency currency = null;

        for (CartItem ci : cart.getItems()) {
            Product p = ci.getProduct();

            if (currency == null) {
                currency = p.getCurrency();
            } else if (currency != p.getCurrency()) {
                throw new IllegalArgumentException("Sepette farklı para birimleri var");
            }

            BigDecimal subtotal = ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProductId(p.getId());
            oi.setProductName(p.getName());
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(ci.getUnitPrice());
            oi.setSellerId(p.getSellerId());
            oi.setSubtotal(subtotal);

            order.getItems().add(oi);
            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
        order.setCurrency(currency);
        Order savedOrder = orderRepository.saveAndFlush(order);
        log.info("Order PENDING oluşturuldu: id={}, total={}", savedOrder.getId(), total);

        // pos-service'e ödeme çağrısı
        try {
            String effectiveKey = (idempotencyKey != null && !idempotencyKey.isBlank())
                    ? idempotencyKey
                    : UUID.randomUUID().toString();

            PosPaymentRequest paymentRequest = new PosPaymentRequest(
                    savedOrder.getId(),
                    request.customerId(),
                    sellerId,
                    request.cardNumber(),
                    total,
                    currency,
                    request.settlementDays(),
                    "Sipariş #" + savedOrder.getId()
            );

            PosPaymentResponse paymentResponse = posServiceClient.pay(effectiveKey, paymentRequest);


            if (paymentResponse.status() == null || !paymentResponse.status().equals("AUTH_OK")) {
                throw new RuntimeException("Ödeme başarısız. POS Durumu: " + paymentResponse.status());
            }

            savedOrder.setStatus(OrderStatus.PAID);
            orderRepository.saveAndFlush(savedOrder);

            // Sepeti temizle
            cart.getItems().clear();
            cartRepository.save(cart);

            log.info("Order başarılı: id={}, bankRef={}", savedOrder.getId(), paymentResponse.bankTransactionRef());

            return new CheckoutResponse(
                    savedOrder.getId(),
                    OrderStatus.PAID,
                    savedOrder.getTotalAmount(),
                    savedOrder.getCurrency(),
                    paymentResponse.bankTransactionRef(),
                    savedOrder.getCreatedAt()
            );

        } catch (FeignException e) {
            savedOrder.setStatus(OrderStatus.FAILED);
            orderRepository.saveAndFlush(savedOrder);
            log.warn("Ödeme başarısız: orderId={}, status={}", savedOrder.getId(), e.status());
            throw e;
        }
    }
}