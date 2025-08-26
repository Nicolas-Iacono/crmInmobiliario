package com.backend.crmInmobiliario.utils;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Converter
public class CryptoAttributeConverter implements AttributeConverter<String, String> {

    @Value("${app.crypto.secret}") // 32 bytes en base64 para AES-256
    private String secretB64;

    private SecretKey key;

    @PostConstruct
    void init() {
        // Creamos la clave AES a partir del secreto en Base64
        byte[] decoded = Base64.getDecoder().decode(secretB64);
        if (decoded.length != 32) {
            throw new IllegalArgumentException("La clave debe ser de 32 bytes (256 bits) en base64");
        }
        this.key = new SecretKeySpec(decoded, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            byte[] iv = new byte[12]; // GCM requiere IV de 12 bytes
            SecureRandom random = SecureRandom.getInstanceStrong();
            random.nextBytes(iv);

            Cipher enc = Cipher.getInstance("AES/GCM/NoPadding");
            enc.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));

            byte[] ct = enc.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            // Guardamos IV + ciphertext en la misma columna
            byte[] out = ByteBuffer.allocate(iv.length + ct.length)
                    .put(iv)
                    .put(ct)
                    .array();

            return Base64.getEncoder().encodeToString(out);

        } catch (Exception e) {
            throw new IllegalStateException("Error al encriptar atributo", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            byte[] all = Base64.getDecoder().decode(dbData);

            byte[] iv = Arrays.copyOfRange(all, 0, 12);
            byte[] ct = Arrays.copyOfRange(all, 12, all.length);

            Cipher dec = Cipher.getInstance("AES/GCM/NoPadding");
            dec.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));

            byte[] pt = dec.doFinal(ct);
            return new String(pt, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new IllegalStateException("Error al desencriptar atributo", e);
        }
    }
}
