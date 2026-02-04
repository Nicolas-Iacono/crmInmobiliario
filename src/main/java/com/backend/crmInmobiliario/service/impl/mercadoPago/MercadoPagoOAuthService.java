package com.backend.crmInmobiliario.service.impl.mercadoPago;

import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class MercadoPagoOAuthService {

    @Value("${mercadopago.oauth.client-id}")
    private String clientId;

    @Value("${mercadopago.oauth.client-secret}")
    private String clientSecret;

    @Value("${mercadopago.oauth.redirect-uri}")
    private String redirectUri;

    private final UsuarioRepository usuarioRepository;
    private final RestTemplate restTemplate;

    public MercadoPagoOAuthService(UsuarioRepository usuarioRepository, RestTemplate restTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Genera el link para que el usuario (inmobiliaria) conecte su MP.
     * state: lo usamos para asociar el callback con el usuario logueado y evitar CSRF.
     */
    public String buildAuthUrl(Long usuarioId) {
        String state = usuarioId + ":" + UUID.randomUUID();

        return UriComponentsBuilder
                .fromHttpUrl("https://auth.mercadopago.com.ar/authorization")
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("platform_id", "mp")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .toUriString();
    }

    /**
     * Intercambia el code por tokens y los guarda en el Usuario.
     */
    @Transactional
    public void connectWithCode(String code, String state) {
        Long usuarioId = parseUserIdFromState(state);

        Usuario user = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Llamada a OAuth token endpoint
        // MP requiere application/x-www-form-urlencoded
        String url = "https://api.mercadopago.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = UriComponentsBuilder.newInstance()
                .queryParam("client_secret", clientSecret)
                .queryParam("client_id", clientId)
                .queryParam("grant_type", "authorization_code")
                .queryParam("code", code)
                .queryParam("redirect_uri", redirectUri)
                .build()
                .toUriString();

        // hack: UriComponentsBuilder te devuelve "?a=b&c=d", lo convertimos a "a=b&c=d"
        String form = body.startsWith("?") ? body.substring(1) : body;

        HttpEntity<String> entity = new HttpEntity<>(form, headers);

        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new RuntimeException("No se pudo obtener tokens de Mercado Pago");
        }

        Map<String, Object> mp = resp.getBody();

        String accessToken = (String) mp.get("access_token");
        String refreshToken = (String) mp.get("refresh_token");
        Object expiresInObj = mp.get("expires_in"); // segundos
        Integer expiresIn = expiresInObj != null ? Integer.parseInt(expiresInObj.toString()) : 0;

        if (accessToken == null || refreshToken == null) {
            throw new RuntimeException("Respuesta OAuth inválida (faltan tokens)");
        }

        user.setMpAccessToken(accessToken);
        user.setMpRefreshToken(refreshToken);
        user.setMpTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
        user.setMpConnected(true);

        usuarioRepository.save(user);

        log.info("✅ Mercado Pago conectado para usuarioId={}", usuarioId);
    }

    /**
     * Refresca token si está vencido o por vencer.
     * (esto lo vas a usar antes de crear una Preference)
     */
    @Transactional
    public String getValidAccessToken(Usuario user) {
        if (!user.isMpConnected() || user.getMpAccessToken() == null || user.getMpRefreshToken() == null) {
            throw new RuntimeException("Mercado Pago no está conectado para este usuario");
        }

        // Si vence en menos de 2 minutos, refrescamos
        if (user.getMpTokenExpiresAt() != null && user.getMpTokenExpiresAt().isAfter(LocalDateTime.now().plusMinutes(2))) {
            return user.getMpAccessToken();
        }

        String url = "https://api.mercadopago.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String form = UriComponentsBuilder.newInstance()
                .queryParam("client_secret", clientSecret)
                .queryParam("client_id", clientId)
                .queryParam("grant_type", "refresh_token")
                .queryParam("refresh_token", user.getMpRefreshToken())
                .build()
                .toUriString();

        form = form.startsWith("?") ? form.substring(1) : form;

        HttpEntity<String> entity = new HttpEntity<>(form, headers);
        ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new RuntimeException("No se pudo refrescar token de Mercado Pago");
        }

        Map<String, Object> mp = resp.getBody();
        String accessToken = (String) mp.get("access_token");
        String refreshToken = (String) mp.get("refresh_token");
        Object expiresInObj = mp.get("expires_in");
        Integer expiresIn = expiresInObj != null ? Integer.parseInt(expiresInObj.toString()) : 0;

        if (accessToken == null) throw new RuntimeException("Refresh inválido: no vino access_token");

        user.setMpAccessToken(accessToken);
        if (refreshToken != null) user.setMpRefreshToken(refreshToken);
        user.setMpTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
        user.setMpConnected(true);

        usuarioRepository.save(user);

        log.info("🔁 Token MP refrescado para usuarioId={}", user.getId());
        return accessToken;
    }

    private Long parseUserIdFromState(String state) {
        if (state == null || !state.contains(":")) {
            throw new RuntimeException("State inválido");
        }
        String part = state.split(":")[0];
        return Long.parseLong(part);
    }
}
