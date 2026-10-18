package com.sanalpos.ecommerce.service;

import com.sanalpos.ecommerce.domain.Cart;
import com.sanalpos.ecommerce.domain.CartItem;
import com.sanalpos.ecommerce.domain.Product;
import com.sanalpos.ecommerce.dto.CartItemRequest;
import com.sanalpos.ecommerce.dto.CartResponse;
import com.sanalpos.ecommerce.exception.ResourceNotFoundException;
import com.sanalpos.ecommerce.repository.CartItemRepository;
import com.sanalpos.ecommerce.repository.CartRepository;
import com.sanalpos.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CartResponse addItem(CartItemRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.productId()));

        if (!product.isActive()) {
            throw new IllegalArgumentException("Ürün aktif değil");
        }

        if (product.getStock() < request.quantity()) {
            throw new IllegalArgumentException("Yetersiz stok. Mevcut: " + product.getStock());
        }

        Cart cart = cartRepository.findByCustomerId(request.customerId())
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setCustomerId(request.customerId());
                    return cartRepository.save(c);
                });

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElseGet(() -> {
                    CartItem ci = new CartItem();
                    ci.setCart(cart);
                    ci.setProduct(product);
                    ci.setQuantity(0);
                    ci.setUnitPrice(product.getPrice());
                    return ci;
                });

        item.setQuantity(item.getQuantity() + request.quantity());
        cartItemRepository.save(item);

        return toResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "customerId", customerId));
        return toResponse(cart);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        List<CartResponse.CartItemResponse> itemResponses = items.stream()
                .map(ci -> new CartResponse.CartItemResponse(
                        ci.getId(),
                        ci.getProduct().getId(),
                        ci.getProduct().getName(),
                        ci.getQuantity(),
                        ci.getUnitPrice(),
                        ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity()))
                ))
                .toList();

        BigDecimal total = itemResponses.stream()
                .map(CartResponse.CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var currency = items.isEmpty() ? null : items.get(0).getProduct().getCurrency();

        return new CartResponse(cart.getId(), cart.getCustomerId(), itemResponses, total, currency);
    }
}