package com.backend.crmInmobiliario.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.impl.GoogleLinkService;
import com.backend.crmInmobiliario.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/oauth/google")
@RequiredArgsConstructor
public class GoogleAccountController {

    private final GoogleLinkService linkService;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final ClientRegistrationRepository clientRegistrationRepository;

    // 1) El front pide authUrl, state y redirectUri basados en el ClientRegistration
    @GetMapping("/link/state")
    public ResponseEntity<Map<String, String>> createState(
            @CookieValue(value = "accessToken", required = false) String accessTokenCookie,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            HttpServletRequest request
    ) {
        String accessToken = resolveAccessToken(accessTokenCookie, authorizationHeader);
        if (accessToken == null || accessToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing access token"));
        }
        DecodedJWT jwt;
        Long userId;
        try {
            jwt = jwtUtil.validateToken(accessToken);
            userId = jwtUtil.extractUserId(jwt);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid access token"));
        }
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token sin userId"));
        }

        ClientRegistration google = (clientRegistrationRepository instanceof InMemoryClientRegistrationRepository)
                ? ((InMemoryClientRegistrationRepository) clientRegistrationRepository).findByRegistrationId("google")
                : null;
        if (google == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registro OAuth2 'google' no encontrado"));
        }

        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString(); // e.g. http://localhost:8080

        String redirectUri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/login/oauth2/code/")
                .path(google.getRegistrationId())
                .build()
                .toUriString(); // http://localhost:8080/login/oauth2/code/google

        // firmamos el state con userId + nonce y expiración corta (5m)
        String nonce = java.util.UUID.randomUUID().toString();

        String state = jwtUtil.createStateToken(String.valueOf(userId), nonce, 5 * 60_000);

        String scopes = String.join(" ", google.getScopes());
        String authUrl = UriComponentsBuilder
                .fromUriString(google.getProviderDetails().getAuthorizationUri())
                .queryParam("client_id", google.getClientId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", scopes)            // tiene espacios -> hay que encodear
                .queryParam("state", state)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .encode(StandardCharsets.UTF_8)         // <-- CLAVE (o usa .build(true))
                .toUriString();

        Map<String, String> body = new HashMap<>();
        body.put("state", state);
        body.put("authUrl", authUrl);
        body.put("redirectUri", redirectUri);
        return ResponseEntity.ok(body);
    }

    // 2) Desvincular la cuenta de Google del usuario logueado
    @DeleteMapping("/link")
    public ResponseEntity<Void> unlink(
            @CookieValue(value = "accessToken", required = false) String accessTokenCookie,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String accessToken = resolveAccessToken(accessTokenCookie, authorizationHeader);
        if (accessToken == null || accessToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing access token");
        }
        DecodedJWT jwt;
        Long userId;
        try {
            jwt = jwtUtil.validateToken(accessToken);
            userId = jwtUtil.extractUserId(jwt);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token");
        }
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token sin userId");
        }
        linkService.unlink(userId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/link/status")
    public Map<String, Boolean> linkStatus(
            @CookieValue(value = "accessToken", required = false) String accessTokenCookie,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String accessToken = resolveAccessToken(accessTokenCookie, authorizationHeader);
        if (accessToken == null || accessToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing access token");
        }
        DecodedJWT jwt;
        Long userId;
        try {
            jwt = jwtUtil.validateToken(accessToken);
            userId = jwtUtil.extractUserId(jwt);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token");
        }
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token sin userId");
        }
        boolean linked = linkService.isLinked(userId);
        return Map.of("linked", linked);
    }

    // 3) Info de la cuenta de Google vinculada (para mostrar en UI)
    @GetMapping("/link/info")
    public Map<String, Object> linkInfo(
            @CookieValue(value = "accessToken", required = false) String accessTokenCookie,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String accessToken = resolveAccessToken(accessTokenCookie, authorizationHeader);
        if (accessToken == null || accessToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing access token");
        }
        DecodedJWT jwt;
        Long userId;
        try {
            jwt = jwtUtil.validateToken(accessToken);
            userId = jwtUtil.extractUserId(jwt);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token");
        }
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token sin userId");
        }

        var acc = linkService.getLinkedAccount(userId);
        boolean linked = acc != null;
        if (!linked) return Map.of("linked", false);

        return Map.of(
                "linked", true,
                "googleId", acc.getGoogleSub(),
                "email", acc.getEmail(),
                "name", acc.getName(),
                "picture", acc.getPictureUrl(),
                "emailVerified", acc.getEmailVerified(),
                "locale", acc.getLocale(),
                "linkedAt", acc.getLinkedAt(),
                "scope", acc.getScope()
        );
    }

    // 4) Perfil consolidado: datos básicos del usuario + estado y datos Google
    @GetMapping("/profile")
    public Map<String, Object> consolidatedProfile(
            @CookieValue(value = "accessToken", required = false) String accessTokenCookie,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String accessToken = resolveAccessToken(accessTokenCookie, authorizationHeader);
        if (accessToken == null || accessToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing access token");
        }
        DecodedJWT jwt;
        Long userId;
        try {
            jwt = jwtUtil.validateToken(accessToken);
            userId = jwtUtil.extractUserId(jwt);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid access token");
        }
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token sin userId");
        }

        Usuario user = usuarioRepository.findById(userId).orElse(null);
        Map<String, Object> result = new HashMap<>();

        if (user != null) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("nombreNegocio", user.getNombreNegocio());
            userMap.put("googleId", user.getGoogleId());
            userMap.put("googleEmail", user.getGoogleEmail());
            result.put("user", userMap);
        }

        var acc = linkService.getLinkedAccount(userId);
        boolean linked = acc != null;
        result.put("googleLinked", linked);
        if (linked) {
            Map<String, Object> googleMap = new HashMap<>();
            googleMap.put("googleId", acc.getGoogleSub());
            googleMap.put("email", acc.getEmail());
            googleMap.put("name", acc.getName());
            googleMap.put("picture", acc.getPictureUrl());
            googleMap.put("emailVerified", acc.getEmailVerified());
            googleMap.put("locale", acc.getLocale());
            googleMap.put("linkedAt", acc.getLinkedAt());
            googleMap.put("scope", acc.getScope());
            result.put("google", googleMap);
        }

        return result;
    }

    private String resolveAccessToken(String cookieToken, String authHeader) {
        if (cookieToken != null && !cookieToken.isBlank()) return cookieToken;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

}
