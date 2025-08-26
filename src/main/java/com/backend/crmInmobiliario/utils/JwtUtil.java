package com.backend.crmInmobiliario.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
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

    public String createToken(Authentication authentication){
        Algorithm algorithm = Algorithm.HMAC256(this.privateKey);

        String username = authentication.getPrincipal().toString();
        String authorities =  authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        String jwtToken = JWT.create()
                .withIssuer(this.userGenerator)
                .withSubject(username)
                .withClaim("authorities", authorities)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis()+3600000))
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis()))
                .sign(algorithm);

        return jwtToken;
    }

    // Overload que incluye el userId como claim para usos donde se necesita recuperarlo (p.ej. estado de Google link)
    public String createToken(Authentication authentication, Long userId){
        Algorithm algorithm = Algorithm.HMAC256(this.privateKey);

        String username = authentication.getPrincipal().toString();
        String authorities =  authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        return JWT.create()
                .withIssuer(this.userGenerator)
                .withSubject(username)
                .withClaim("authorities", authorities)
                .withClaim("userId", userId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis()+3600000))
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis()))
                .sign(algorithm);
    }

    public DecodedJWT validateToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(this.privateKey);

            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(this.userGenerator)
                    .build();

            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT;
        }catch (JWTVerificationException exception){
            throw new JWTVerificationException("TOKEN INVALIDO, NO AUTORIZADO");
        }
    }

    public String extractUsername(DecodedJWT decodedJWT){
        return decodedJWT.getSubject().toString();
    }

    public Long extractUserId(DecodedJWT decodedJWT){
        return decodedJWT.getClaim("userId").asLong();
    }
    public Claim getSpecifClaim(DecodedJWT decodedJWT, String claimName){
        return decodedJWT.getClaim(claimName);
    }
    public Map<String, Claim> returnAllClaims(DecodedJWT decodedJWT){
        return decodedJWT.getClaims();
    }



    // Para Google OAuth2, donde ya tenés el email y roles desde Google

    public String createTokenFromGoogleUser(String email, String authorities) {
        Algorithm algorithm = Algorithm.HMAC256(this.privateKey);

        return JWT.create()
                .withIssuer(this.userGenerator)
                .withSubject(email)  // el email de Google
                .withClaim("authorities", authorities) // ej: "ROLE_USER"
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 3600000))
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(System.currentTimeMillis()))
                .sign(algorithm);
    }

    // ==== 2) Token de STATE para el linking con Google ====
    // Corto (ej. 5 minutos). Lleva userId y un nonce para validar que vuelve la misma sesión.
    public String createStateToken(String userId, String nonce, long ttlMillis) {
        Algorithm algorithm = Algorithm.HMAC256(this.privateKey);
        long now = System.currentTimeMillis();
        return JWT.create()
                .withIssuer(this.userGenerator)
                .withSubject(userId)              // el userId de TU app (no el de Google)
                .withClaim("flow", "google_link") // etiqueta el uso
                .withClaim("nonce", nonce)
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(now + ttlMillis)) // ej. 5 min
                .withJWTId(UUID.randomUUID().toString())
                .withNotBefore(new Date(now))
                .sign(algorithm);
    }

    // Validación del state (y de su nonce)
    public DecodedJWT validateStateToken(String token, String expectedNonce){
        DecodedJWT jwt = validateToken(token); // reutiliza tu validador
        String flow = jwt.getClaim("flow").asString();
        String nonce = jwt.getClaim("nonce").asString();
        if (!"google_link".equals(flow) || nonce == null || !nonce.equals(expectedNonce)) {
            throw new JWTVerificationException("STATE inválido");
        }
        return jwt;
    }

}
