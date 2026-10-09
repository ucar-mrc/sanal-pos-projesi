package com.sanalpos.ecommerce.service;

import com.sanalpos.ecommerce.domain.Product;
import com.sanalpos.ecommerce.dto.ProductRequest;
import com.sanalpos.ecommerce.dto.ProductResponse;
import com.sanalpos.ecommerce.exception.ResourceNotFoundException;
import com.sanalpos.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        product.setSellerId(request.sellerId());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCurrency(request.currency());
        product.setStock(request.stock());
        product.setActive(true);

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllActive() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getBySeller(Long sellerId) {
        return productRepository.findBySellerIdAndActiveTrue(sellerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setSellerId(request.sellerId());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCurrency(request.currency());
        product.setStock(request.stock());

        // saveAndFlush: Hibernate'i hemen flush etmeye zorlar, böylece @PreUpdate çalışır
        Product saved = productRepository.saveAndFlush(product);
        return toResponse(saved);
    }

    @Transactional
    public void deactivate(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setActive(false);
    }

    // =====================================================
    // YARDIMCI
    // =====================================================
    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSellerId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency(),
                product.getStock(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}