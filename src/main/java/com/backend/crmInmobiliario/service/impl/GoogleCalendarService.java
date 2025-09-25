package com.backend.crmInmobiliario.service.impl;

import com.backend.crmInmobiliario.entity.UsuarioGoogleAccount;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioGoogleAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    @Qualifier("googleApis")
    private final WebClient googleApis;

    @Qualifier("googleOAuth")
    private final WebClient googleOAuth;

    private final UsuarioGoogleAccountRepository googleRepo;

    // Elegí UNO de estos bloques @Value según tus properties reales:

    // a) Si definiste claves cortas
    @Value("${google.client.id}")
    private String clientId;
    @Value("${google.client.secret}")
    private String clientSecret;

    // b) Si usás las de Spring Security (descomenta éstas y comenta las de arriba)
    // @Value("${spring.security.oauth2.client.registration.google.client-id}")
    // private String clientId;
    // @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    // private String clientSecret;

    /** Lista eventos entre from y to del calendario indicado (o "primary"). */
    public List<Map<String,Object>> listEvents(Long userId, String calendarId, Instant from, Instant to) {
        var acc = googleRepo.findByUsuarioId(userId)
                .orElseThrow(() -> new IllegalStateException("La cuenta de Google no está vinculada"));
        String token = ensureAccessToken(acc);

        String cal = (calendarId == null || calendarId.isBlank()) ? "primary" : calendarId;

        List<Map<String,Object>> out = new ArrayList<>();
        String pageToken = null;

        do {
            var uri = UriComponentsBuilder
                    .fromUriString("https://www.googleapis.com/calendar/v3/calendars/{cal}/events")
                    .queryParam("timeMin", from.toString())
                    .queryParam("timeMax", to.toString())
                    .queryParam("timeZone", "America/Argentina/Buenos_Aires")
                    .queryParam("singleEvents", "true")
                    .queryParam("orderBy", "startTime");
            if (pageToken != null) uri.queryParam("pageToken", pageToken);

            Map<String,Object> resp = googleApis.get()
                    .uri(uri.buildAndExpand(cal).toUri())
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String,Object>>() {})
                    .block();

            if (resp == null) break;

            @SuppressWarnings("unchecked")
            List<Map<String,Object>> items = (List<Map<String,Object>>) resp.getOrDefault("items", List.of());

            for (Map<String,Object> ev : items) {
                @SuppressWarnings("unchecked") Map<String,Object> start = (Map<String,Object>) ev.get("start");
                @SuppressWarnings("unchecked") Map<String,Object> end   = (Map<String,Object>) ev.get("end");

                out.add(new HashMap<>() {{
                    put("id", ev.get("id"));
                    put("summary", ev.get("summary"));
                    put("description", ev.get("description")); // puede quedar null sin romper
                    put("start", start);
                    put("end", end);
                    put("status", ev.get("status"));
                    put("htmlLink", ev.get("htmlLink"));
                    put("hangoutLink", ev.get("hangoutLink"));
                }});
            }

            pageToken = (String) resp.get("nextPageToken");
        } while (pageToken != null);

        return out;
    }

    /** Crea un evento en calendarId (o primary). */
    public Map<String, Object> createEvent(Long userId, String calendarId, Map<String,Object> body) {
        var acc = googleRepo.findByUsuarioId(userId)
                .orElseThrow(() -> new IllegalStateException("No vinculado"));
        String token = ensureAccessToken(acc);
        String cal = (calendarId == null || calendarId.isBlank()) ? "primary" : calendarId;

        return googleApis.post()
                .uri("/calendar/v3/calendars/{cal}/events", cal)
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)   // directamente el Map que ya tiene summary, start, end
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String,Object>>() {})
                .block();
    }


    /** Borra un evento. */
    public void deleteEvent(Long userId, String calendarId, String eventId) {
        var acc = googleRepo.findByUsuarioId(userId)
                .orElseThrow(() -> new IllegalStateException("No vinculado"));
        String token = ensureAccessToken(acc);
        String cal = (calendarId == null || calendarId.isBlank()) ? "primary" : calendarId;

        googleApis.delete()
                .uri("/calendar/v3/calendars/{cal}/events/{id}", cal, eventId)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    /** Garantiza access token válido; si venció, lo refresca con refresh_token. */
    private String ensureAccessToken(UsuarioGoogleAccount acc) {
        // válido con 60s de colchón
        if (acc.getAccessToken() != null &&
                acc.getAccessTokenExpiresAt() != null &&
                acc.getAccessTokenExpiresAt().after(new Date(System.currentTimeMillis() + 60_000))) {
            return acc.getAccessToken();
        }

        if (acc.getRefreshToken() == null || acc.getRefreshToken().isBlank()) {
            throw new IllegalStateException("No hay refresh token para renovar el access token de Google");
        }

        var form = new LinkedMultiValueMap<String,String>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", acc.getRefreshToken());

        Map<String,Object> resp = googleOAuth.post()
                .uri("/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String,Object>>() {})
                .block();

        if (resp == null || !resp.containsKey("access_token")) {
            throw new IllegalStateException("No se pudo refrescar el token de Google");
        }

        String newAccess = (String) resp.get("access_token");
        int expiresIn = ((Number) resp.getOrDefault("expires_in", 3600)).intValue();

        acc.setAccessToken(newAccess);
        acc.setAccessTokenExpiresAt(new Date(System.currentTimeMillis() + expiresIn * 1000L));
        googleRepo.save(acc);

        return newAccess;
    }

    private static String extractDateOrDateTime(Map<String,Object> node) {
        if (node == null) return null;
        Object dt = node.get("dateTime");  // 2025-08-27T10:00:00-03:00
        if (dt == null) dt = node.get("date"); // 2025-08-27
        return dt != null ? dt.toString() : null;
    }
}
