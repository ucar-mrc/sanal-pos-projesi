package com.sanalpos.ecommerce.domain;

public enum OrderStatus {
    PENDING,      // oluşturuldu, ödeme bekliyor
    PAID,         // ödeme alındı
    FAILED,       // ödeme başarısız
    CANCELLED,    // iptal edildi
    REFUNDED      // iade edildi
}