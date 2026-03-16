package com.backend.crmInmobiliario.utils.signature;

import java.security.SecureRandom;

public final class SignatureOtpUtil {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SignatureOtpUtil() {
    }

    public static String generateSixDigits() {
        int value = SECURE_RANDOM.nextInt(900_000) + 100_000;
        return String.valueOf(value);
    }
}
