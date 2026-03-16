package com.backend.crmInmobiliario.utils.signature;

import java.security.SecureRandom;
import java.util.Base64;

public final class SignatureTokenUtil {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SignatureTokenUtil() {
    }

    public static String generateUrlSafeToken() {
        byte[] data = new byte[32];
        SECURE_RANDOM.nextBytes(data);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
}
