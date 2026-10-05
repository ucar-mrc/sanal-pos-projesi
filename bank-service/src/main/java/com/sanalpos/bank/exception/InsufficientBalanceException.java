package com.sanalpos.bank.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(BigDecimal current, BigDecimal requested) {
        super(String.format("Yetersiz bakiye. Mevcut: %s, istenen: %s", current, requested));
    }
}