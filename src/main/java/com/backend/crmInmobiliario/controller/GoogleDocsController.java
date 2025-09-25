package com.backend.crmInmobiliario.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.backend.crmInmobiliario.service.impl.GoogleDocsService;
import com.backend.crmInmobiliario.service.impl.GoogleLinkService;
import com.backend.crmInmobiliario.utils.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/google/docs")
@RequiredArgsConstructor
public class GoogleDocsController {

    private final JwtUtil jwtUtil;
    private final GoogleLinkService linkService;
    private final GoogleDocsService googleDocsService;

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    // Le mandás { "title": "Contrato X", "html": "<div>...</div>" }
    @PostMapping("/from-html")
    public ResponseEntity<Map<String, String>> createFromHtml(
            @CookieValue(value = "accessToken", required = false) String accessTokenCookie,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody CreateDocRequest body
    ) throws Exception {

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

        var acc = linkService.getLinkedAccount(userId);
        if (acc == null) {
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
                    "No hay cuenta de Google vinculada");
        }
        // Debés tener guardado refresh_token y último access_token
        String googleAccessToken = acc.getAccessToken();

        var result = googleDocsService.createDocFromHtml(
                userId,
                body.getHtml(),
                body.getTitle(),
                googleAccessToken,
                acc.getRefreshToken(),
                clientId,
                clientSecret
        );

        return ResponseEntity.ok(result);
    }

    private String resolveAccessToken(String cookieToken, String authHeader) {
        if (cookieToken != null && !cookieToken.isBlank()) return cookieToken;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    @Data
    public static class CreateDocRequest {
        private String title;
        private String html;
    }
}
