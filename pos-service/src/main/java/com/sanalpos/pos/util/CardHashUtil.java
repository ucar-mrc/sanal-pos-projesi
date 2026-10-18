package com.sanalpos.pos.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class CardHashUtil {

    private CardHashUtil() {
    }

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public static String last4(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            throw new IllegalArgumentException("Kart numarası en az 4 haneli olmalı");
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }

    public static String normalize(String cardNumber) {
        if (cardNumber == null) return null;
        return cardNumber.replaceAll("\\s+", "");
    }
}