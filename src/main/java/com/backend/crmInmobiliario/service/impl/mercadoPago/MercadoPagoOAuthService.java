package com.backend.crmInmobiliario.service.impl.mercadoPago;


import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class MercadoPagoOAuthService {

    @Value("${mp.client.id}")
    private String clientId;

    @Value("${mp.client.secret}")
    private String clientSecret;

    private final UsuarioRepository usuarioRepository;
    private final RestTemplate restTemplate;

    public MercadoPagoOAuthService(UsuarioRepository usuarioRepository, RestTemplate restTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.restTemplate = restTemplate;
    }

    public String getValidAccessToken(Usuario u) {
        if (u == null || !u.isMpConnected() || u.getMpAccessToken() == null) {
            throw new IllegalStateException("Mercado Pago no conectado");
        }

        // si no sabemos expiración, usamos el token actual
        if (u.getMpTokenExpiresAt() == null) return u.getMpAccessToken();

        // refrescar 60s antes del vencimiento
        if (LocalDateTime.now().isBefore(u.getMpTokenExpiresAt().minusSeconds(60))) {
            return u.getMpAccessToken();
        }

        if (u.getMpRefreshToken() == null) {
            throw new IllegalStateException("No hay refresh_token para renovar el token de Mercado Pago");
        }

        String tokenUrl = "https://api.mercadopago.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", u.getMpRefreshToken());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        Map<String, Object> resp = restTemplate.postForObject(tokenUrl, entity, Map.class);
        if (resp == null || !resp.containsKey("access_token")) {
            throw new IllegalStateException("No se pudo refrescar access_token de Mercado Pago");
        }

        u.setMpAccessToken(String.valueOf(resp.get("access_token")));

        if (resp.get("refresh_token") != null) {
            u.setMpRefreshToken(String.valueOf(resp.get("refresh_token")));
        }

        if (resp.get("expires_in") != null) {
            int expiresIn = Integer.parseInt(String.valueOf(resp.get("expires_in")));
            u.setMpTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
        }

        usuarioRepository.save(u);
        return u.getMpAccessToken();
    }
}
