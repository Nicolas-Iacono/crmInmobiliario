package com.backend.crmInmobiliario.controller;


import com.backend.crmInmobiliario.DTO.googleOuthApi.GoogleTokenResponseOuth;
import com.backend.crmInmobiliario.DTO.googleOuthApi.GoogleUserInfo;
import com.backend.crmInmobiliario.service.impl.GoogleLinkService;
import com.backend.crmInmobiliario.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@RestController
@RequestMapping("/rest/oauth2-credential")
public class GoogleLinkCallbackController {
    @Value("${google.client.id}")     String clientId;
    @Value("${google.client.secret}") String clientSecret;
    @Value("${google.redirect.uri}")  String redirectUri;

    @Value("${app.frontend.callback:https://tuinmo.net/ajustes?google_link=ok}")
    String frontendOk;
    @Value("${app.frontend.callback.error:https://tuinmo.net/ajustes?google_link=error}")
    String frontendError;

    private final RestTemplate http = new RestTemplate();
    private final JwtUtil jwtUtil;
    private final GoogleLinkService linkService;

    public GoogleLinkCallbackController(JwtUtil jwtUtil, GoogleLinkService linkService) {
        this.jwtUtil = jwtUtil; this.linkService = linkService;
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam String code,
                                         @RequestParam(required = false) String state) {
        try {
            // 1) validar state (recomendado: JWT con subject=userId y nonce)
            Long userId = null;
            if (state != null) {
                var decoded = jwtUtil.validateToken(state);
                userId = Long.valueOf(decoded.getSubject()); // el subject del state = userId de TU app
            } else {
                throw new IllegalArgumentException("Falta state");
            }

            // 2) code -> tokens
            var tokens = exchangeCodeForTokens(code);

            // 3) obtener perfil
            var userInfo = fetchUserInfo(tokens.access_token());

            // (opcional) cruzar sub de id_token con /userinfo
            // Map<String,Object> idp = JwtUtils.decodePayload(tokens.id_token());
            // if (!userInfo.sub().equals(String.valueOf(idp.get("sub")))) throw new IllegalStateException("sub mismatch");

            // 4) persistir vinculación
            linkService.linkWithInfo(userId, userInfo, tokens);

            // 5) redirigir al front (limpio, sin tokens)
            HttpHeaders h = new HttpHeaders();
            h.setLocation(URI.create(frontendOk));
            return new ResponseEntity<>(h, HttpStatus.FOUND);

        } catch (Exception e) {
            HttpHeaders h = new HttpHeaders();
            h.setLocation(URI.create(frontendError));
            return new ResponseEntity<>(h, HttpStatus.FOUND);
        }
    }

    private GoogleTokenResponseOuth exchangeCodeForTokens(String code) {
        String url = "https://oauth2.googleapis.com/token";
        var form = new LinkedMultiValueMap<String, String>();
        form.add("code", code);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("redirect_uri", redirectUri); // DEBE coincidir 1:1 con el usado en el botón y autorizado en Google
        form.add("grant_type", "authorization_code");

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        var res = http.postForEntity(url, new HttpEntity<>(form, headers), GoogleTokenResponseOuth.class);
        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null)
            throw new IllegalStateException("Error al intercambiar code");
        return res.getBody();
    }

    private GoogleUserInfo fetchUserInfo(String accessToken) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        var res = http.exchange("https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET, new HttpEntity<>(headers), GoogleUserInfo.class);
        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null)
            throw new IllegalStateException("No se pudo obtener userinfo");
        return res.getBody();
    }
}
