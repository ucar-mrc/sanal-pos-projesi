package com.sanalpos.ecommerce.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resource, String field, Object value) {
        super(String.format("%s zaten mevcut: %s = %s", resource, field, value));
    }
}