package com.backend.crmInmobiliario.service.impl.mercadoPago;

import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class MercadoPagoOAuthConnectService {

    @Value("${mp.client.id}") private String clientId;
    @Value("${mp.client.secret}") private String clientSecret;
    @Value("${mp.redirect.uri}") private String redirectUri;

    private final UsuarioRepository usuarioRepository;
    private final RestTemplate restTemplate;

    public void exchangeCodeAndSave(String code, String state) {
        Long userId = parseUserIdFromState(state);

        Usuario u = usuarioRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String tokenUrl = "https://api.mercadopago.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        Map<String, Object> resp = restTemplate.postForObject(tokenUrl, entity, Map.class);
        if (resp == null || !resp.containsKey("access_token")) {
            throw new RuntimeException("No se pudo obtener access_token de Mercado Pago");
        }

        u.setMpAccessToken(String.valueOf(resp.get("access_token")));
        u.setMpRefreshToken(String.valueOf(resp.get("refresh_token")));
        u.setMpConnected(true);

        if (resp.get("user_id") != null) {
            u.setMpUserId(String.valueOf(resp.get("user_id")));
        }

        if (resp.get("expires_in") != null) {
            int expiresIn = Integer.parseInt(String.valueOf(resp.get("expires_in")));
            u.setMpTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
        }

        usuarioRepository.save(u);
    }

    private Long parseUserIdFromState(String state) {
        if (state == null || !state.startsWith("u:")) throw new RuntimeException("state inválido");
        return Long.parseLong(state.substring(2));
    }
}

