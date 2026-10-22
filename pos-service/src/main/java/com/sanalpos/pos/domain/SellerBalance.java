package com.sanalpos.pos.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "seller_balances")
@Getter
@Setter
@NoArgsConstructor
public class SellerBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false, unique = true)
    private Long sellerId;

    @Column(name = "available_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(name = "pending_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal pendingBalance = BigDecimal.ZERO;

    @Column(name = "total_withdrawn", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalWithdrawn = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}