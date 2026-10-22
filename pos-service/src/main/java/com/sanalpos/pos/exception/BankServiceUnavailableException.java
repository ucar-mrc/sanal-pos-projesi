package com.sanalpos.pos.exception;

public class BankServiceUnavailableException extends RuntimeException {
    public BankServiceUnavailableException(String message) {
        super(message);
    }
}