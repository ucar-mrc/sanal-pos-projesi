package com.sanalpos.bank.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class CardHashUtil {

    private CardHashUtil() {
        // utility class — instantiate edilmesin
    }

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 her JVM'de vardır, buraya düşmez
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public static String last4(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            throw new IllegalArgumentException("Kart numarası en az 4 haneli olmalı");
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * Kart numarasındaki boşlukları temizler. 16 haneli kart "1234 5678 9012 3456"
     * formatında gelebilir → normalize edip "1234567890123456" yapar.
     */
    public static String normalize(String cardNumber) {
        if (cardNumber == null) return null;
        return cardNumber.replaceAll("\\s+", "");
    }
}