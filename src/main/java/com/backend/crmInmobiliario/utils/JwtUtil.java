package com.backend.crmInmobiliario.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${security.jwt.key.private}")
    private String privateKey;
    @Value("${security.jwt.user.generator}")
    private String userGenerator;

    // ====== Config ======
    private static final long SKEW_MS = 5_000;               // tolerancia local
    private static final long ACCESS_TTL_MS = 3 * 60 * 60 * 1000;
    ;   // 3 horas
    private static final long REFRESH_TTL_MS = 7L * 24 * 60 * 60 * 1000; // 7 días

    private Algorithm alg() {
        return Algorithm.HMAC256(this.privateKey);
    }

    // ====== Create ======
//    public String createAccessToken(String username, String authorities) {
//        long now = System.currentTimeMillis();
//        return JWT.create()
//                .withIssuer(userGenerator)
//                .withSubject(username)
//                .withClaim("authorities", authorities)   // "ROLE_USER,ROLE_ADMIN"
//                .withIssuedAt(new Date(now))
//                .withNotBefore(new Date(now - SKEW_MS))
//                .withExpiresAt(new Date(now + ACCESS_TTL_MS))
//                .withJWTId(UUID.randomUUID().toString())
//                .sign(alg());
//    }

//    public String createAccessToken(Authentication auth) {
//        String username = auth.getPrincipal().toString();
//        String authorities = auth.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.joining(","));
//        return createAccessToken(username, authorities);
//    }

    public String createRefreshToken(String username) {
        long now = System.currentTimeMillis();
        return JWT.create()
                .withIssuer(userGenerator)
                .withSubject(username)
                .withClaim("typ", "refresh")
                .withIssuedAt(new Date(now))
                .withNotBefore(new Date(now - SKEW_MS))
                .withExpiresAt(new Date(now + REFRESH_TTL_MS))
                .withJWTId(UUID.randomUUID().toString())
                .sign(alg());
    }

    public String createRefreshToken(Authentication auth) {
        String username;
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            username = userDetails.getUsername(); // ✅ limpio
        } else {
            username = principal.toString();
        }
        return createRefreshToken(username);
    }

    // Variante con userId (si lo necesitas en el access)
    public String createAccessToken(Authentication auth, Long userId) {
        long now = System.currentTimeMillis();

        String username;
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            username = userDetails.getUsername(); // ✅ email limpio
        } else {
            username = principal.toString();
        }

        String authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return JWT.create()
                .withIssuer(userGenerator)
                .withSubject(username)
                .withClaim("authorities", authorities)
                .withClaim("userId", userId)
                .withIssuedAt(new Date(now))
                .withNotBefore(new Date(now - SKEW_MS))
                .withExpiresAt(new Date(now + ACCESS_TTL_MS))
                .withJWTId(UUID.randomUUID().toString())
                .sign(alg());
    }

    // ====== Validate (separadas) ======
    public DecodedJWT validateAccessToken(String token) {
        DecodedJWT jwt = baseVerifier().verify(token);
        // si por error te mandan un refresh en un endpoint protegido, lo rechazamos
        if ("refresh".equals(jwt.getClaim("typ").asString())) {
            throw new JWTVerificationException("Refresh token no permitido para este recurso");
        }
        return jwt;
    }

    public DecodedJWT validateRefreshToken(String token) {
        DecodedJWT jwt = baseVerifier().verify(token);
        if (!"refresh".equals(jwt.getClaim("typ").asString())) {
            throw new JWTVerificationException("Se esperaba un refresh token");
        }
        return jwt;
    }

    // Si quieres mantener un único validador genérico:
    public DecodedJWT validateToken(String token) {
        return baseVerifier().verify(token);
    }

    private JWTVerifier baseVerifier() {
        return JWT.require(alg())
                .withIssuer(userGenerator)
                .acceptLeeway(60) // 60s tolerancia entre relojes
                .build();
    }

    // ====== Extractores ======
    public String extractUsername(DecodedJWT decodedJWT) {
        return decodedJWT.getSubject();
    }

    public Long extractUserId(DecodedJWT decodedJWT) {
        return decodedJWT.getClaim("userId").asLong();
    }

    public Claim getSpecifClaim(DecodedJWT decodedJWT, String claimName) {
        return decodedJWT.getClaim(claimName);
    }

    public Map<String, Claim> returnAllClaims(DecodedJWT decodedJWT) {
        return decodedJWT.getClaims();
    }

    // ====== Google OAuth2 (opcional) ======
    public String createTokenFromGoogleUser(String email, String authorities) {
        long now = System.currentTimeMillis();
        return JWT.create()
                .withIssuer(this.userGenerator)
                .withSubject(email)
                .withClaim("authorities", authorities)
                .withIssuedAt(new Date(now))
                .withNotBefore(new Date(now - SKEW_MS))
                .withExpiresAt(new Date(now + ACCESS_TTL_MS))
                .withJWTId(UUID.randomUUID().toString())
                .sign(alg());
    }

    // ====== STATE para linking con Google ======
    public String createStateToken(String userId, String nonce, long ttlMillis) {
        long now = System.currentTimeMillis();
        return JWT.create()
                .withIssuer(userGenerator)
                .withSubject(userId)
                .withClaim("flow", "google_link")
                .withClaim("nonce", nonce)
                .withIssuedAt(new Date(now))
                .withNotBefore(new Date(now - SKEW_MS))
                .withExpiresAt(new Date(now + ttlMillis))
                .withJWTId(UUID.randomUUID().toString())
                .sign(alg());
    }

    public DecodedJWT validateStateToken(String token, String expectedNonce) {
        DecodedJWT jwt = baseVerifier().verify(token);
        String flow = jwt.getClaim("flow").asString();
        String nonce = jwt.getClaim("nonce").asString();
        if (!"google_link".equals(flow) || nonce == null || !nonce.equals(expectedNonce)) {
            throw new JWTVerificationException("STATE inválido");
        }
        return jwt;
    }

    public Long extractUserIdFromAuth(Authentication auth) {
        if (auth == null) return null;

        Object details = auth.getDetails();
        if (details instanceof Map<?, ?> map) {
            Object id = map.get("userId");
            if (id instanceof Long l) return l;
            if (id instanceof Integer i) return i.longValue();
            if (id instanceof String s) return Long.parseLong(s);
        }

        return null;
    }

    public String getUsernameFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token JWT no encontrado o inválido en el header Authorization");
        }

        String token = authHeader.substring(7); // Elimina "Bearer "
        DecodedJWT decodedJWT = validateToken(token); // Usa tu validador ya existente
        return extractUsername(decodedJWT);
    }
}