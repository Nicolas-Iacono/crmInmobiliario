package com.backend.crmInmobiliario.controller.providerControllers;

import com.backend.crmInmobiliario.service.impl.mercadoPago.MercadoPagoOAuthConnectService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/mercadopago/oauth")
@RequiredArgsConstructor
public class MercadoPagoOAuthController {

    @Value("${mp.client.id}")
    private String clientId;

    @Value("${mp.redirect.uri}")
    private String redirectUri;

    private final AuthUtil authUtil;
    private final MercadoPagoOAuthConnectService connectService;

    @GetMapping("/authorize")
    public ResponseEntity<Map<String, String>> authorize() {
        Long userId = authUtil.extractUserId();

        // state sirve para volver y saber quién conectó
        String state = "u:" + userId;

        String url = "https://auth.mercadopago.com.ar/authorization"
                + "?client_id=" + clientId
                + "&response_type=code"
                + "&platform_id=mp"
                + "&redirect_uri=" + redirectUri
                + "&state=" + state;

        return ResponseEntity.ok(Map.of("auth_url", url));
    }

    @GetMapping("/callback")
    public ResponseEntity<String> callback(@RequestParam String code, @RequestParam(required = false) String state) {
        connectService.exchangeCodeAndSave(code, state);
        // redirigí a tu panel
        return ResponseEntity.status(302)
                .header("Location", "https://tuinmo.net/panel?mp=ok")
                .build();
    }
}

