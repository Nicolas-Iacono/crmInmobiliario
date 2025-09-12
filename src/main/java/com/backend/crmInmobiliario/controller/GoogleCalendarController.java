package com.backend.crmInmobiliario.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.backend.crmInmobiliario.service.impl.GoogleCalendarService;
import com.backend.crmInmobiliario.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/google/calendar")
@RequiredArgsConstructor
public class GoogleCalendarController {

    private final GoogleCalendarService calendarService;
    private final JwtUtil jwtUtil;

    // ---------- GET /google/calendar/events?from=ISO&to=ISO[&calendarId=xxx] ----------
    @GetMapping("/events")
    public ResponseEntity<?> listEvents(
            @CookieValue(value = "accessToken", required = false) String accessTokenCookie,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false) String calendarId
    ) {
        Long userId = extractUserIdOr401(accessTokenCookie, authorizationHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing or invalid access token"));
        }

        try {
            Instant timeMin = parseInstantFlexible(from);
            Instant timeMax = parseInstantFlexible(to);

            List<Map<String, Object>> events =
                    calendarService.listEvents(userId, calendarId, timeMin, timeMax);

            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudieron obtener los eventos", "detail", e.getMessage()));
        }
    }

    // ---------- POST /google/calendar/events  (crear evento en primary por defecto) ----------
    @PostMapping("/events")
    public ResponseEntity<?> createEvent(
            @CookieValue(value = "accessToken", required = false) String accessTokenCookie,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(required = false) String calendarId,
            @RequestBody Map<String, Object> body
    ) {
        Long userId = extractUserIdOr401(accessTokenCookie, authorizationHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing or invalid access token"));
        }

        // Validación básica del body
        if (body == null || !body.containsKey("summary")
                || !body.containsKey("start") || !body.containsKey("end")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El body debe incluir summary, start y end",
                            "example", Map.of(
                                    "summary", "Reunión",
                                    "description", "Demo",
                                    "start", Map.of("dateTime", "2025-08-27T10:00:00-03:00"),
                                    "end", Map.of("dateTime", "2025-08-27T11:00:00-03:00")
                            )));
        }

        try {
            Map<String, Object> created = calendarService.createEvent(userId, calendarId, body);

            // Google devuelve un objeto con "id" y "htmlLink"
            String eventId = (String) created.get("id");
            String htmlLink = (String) created.get("htmlLink");

            URI location = URI.create(
                    String.format("/google/calendar/events/%s", eventId)
            );

            return ResponseEntity.created(location)
                    .body(Map.of(
                            "message", "Evento creado con éxito",
                            "id", eventId,
                            "htmlLink", htmlLink,
                            "event", created
                    ));

        } catch (WebClientResponseException e) {
            // Errores que vienen de Google
            return ResponseEntity.status(e.getRawStatusCode())
                    .body(Map.of("error", "Google Calendar API error",
                            "status", e.getRawStatusCode(),
                            "body", e.getResponseBodyAsString()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo crear el evento",
                            "detail", e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    // Extraer userId de la cookie o header


    // ---------- DELETE /google/calendar/events/{eventId} ----------
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<?> deleteEvent(
            @CookieValue(value = "accessToken", required = false) String accessTokenCookie,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable String eventId,
            @RequestParam(required = false) String calendarId
    ) {
        Long userId = extractUserIdOr401(accessTokenCookie, authorizationHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing or invalid access token"));
        }

        try {
            calendarService.deleteEvent(userId, calendarId, eventId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo eliminar el evento", "detail", e.getMessage()));
        }
    }

    // ---------- Helpers ----------
    private Long extractUserIdOr401(String cookieToken, String authHeader) {
        String accessToken = resolveAccessToken(cookieToken, authHeader);
        if (accessToken == null || accessToken.isBlank()) return null;
        try {
            DecodedJWT jwt = jwtUtil.validateToken(accessToken);
            return jwtUtil.extractUserId(jwt);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String resolveAccessToken(String cookieToken, String authHeader) {
        if (cookieToken != null && !cookieToken.isBlank()) return cookieToken;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    // Parsing flexible de fechas para soportar Z, offset, sin zona y solo fecha
    private Instant parseInstantFlexible(String s) {
        if (s == null) throw new IllegalArgumentException("from/to es requerido");
        try {
            return Instant.parse(s); // soporta "2025-08-27T10:00:00Z"
        } catch (Exception ignored) { }
        try {
            return OffsetDateTime.parse(s).toInstant(); // soporta con offset "-03:00"
        } catch (Exception ignored) { }
        try {
            return LocalDateTime.parse(s).toInstant(ZoneOffset.UTC); // soporta "2025-08-27T10:00:00"
        } catch (Exception ignored) { }
        try {
            return LocalDate.parse(s).atStartOfDay(ZoneOffset.UTC).toInstant(); // soporta solo fecha
        } catch (Exception ignored) { }
        throw new IllegalArgumentException("Formato de fecha inválido: " + s);
    }
}
