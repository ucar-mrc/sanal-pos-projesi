package com.sanalpos.bank.domain;

public enum TransactionType {
    DEBIT,           // hesaptan para çıktı
    CREDIT,          // hesaba para girdi
    TRANSFER_IN,     // transfer sonucu hesaba girdi
    TRANSFER_OUT     // transfer sonucu hesaptan çıktı
}