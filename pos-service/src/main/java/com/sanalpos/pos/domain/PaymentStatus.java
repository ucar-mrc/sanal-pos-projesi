package com.sanalpos.pos.domain;

public enum PaymentStatus {
    PENDING,      // oluşturuldu, henüz bank'a gitmedi
    AUTH_OK,      // banka onayladı, para çekildi
    FAILED,       // banka reddetti (yetersiz bakiye vs.)
    REFUNDED      // iade edildi
}