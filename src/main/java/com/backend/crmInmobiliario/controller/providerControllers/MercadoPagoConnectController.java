package com.backend.crmInmobiliario.controller.providerControllers;

import com.backend.crmInmobiliario.DTO.mpDtos.OAuth.MpConnectStatusResponse;
import com.backend.crmInmobiliario.DTO.mpDtos.OAuth.MpOAuthUrlResponse;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.repository.USER_REPO.UsuarioRepository;
import com.backend.crmInmobiliario.service.impl.mercadoPago.MercadoPagoOAuthService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/mercadopago")
@RequiredArgsConstructor
public class MercadoPagoConnectController {

    private final AuthUtil authUtil;
    private final UsuarioRepository usuarioRepository;
    private final MercadoPagoOAuthService mpOAuthService;

    @Value("${mercadopago.front.success-redirect:}")
    private String frontSuccessRedirect;

    @Value("${mercadopago.front.error-redirect:}")
    private String frontErrorRedirect;

    /**
     * Paso 1: el usuario inmobiliaria pide la URL para conectar su MP.
     */
    @GetMapping("/connect-url")
    public ResponseEntity<MpOAuthUrlResponse> getConnectUrl() {
        Long userId = authUtil.extractUserId(); // inmobiliaria logueada
        String url = mpOAuthService.buildAuthUrl(userId);
        return ResponseEntity.ok(new MpOAuthUrlResponse(url));
    }

    /**
     * Paso 2: Mercado Pago redirige a tu redirect-uri con ?code=...&state=...
     * Este endpoint debe ser el redirect-uri o un endpoint que tu front llame con esos params.
     */
    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam String code, @RequestParam String state) {
        try {
            mpOAuthService.connectWithCode(code, state);

            // Si querés redirigir a tu front:
            if (frontSuccessRedirect != null && !frontSuccessRedirect.isBlank()) {
                return ResponseEntity.status(302).location(URI.create(frontSuccessRedirect)).build();
            }

            return ResponseEntity.ok().body(java.util.Map.of("ok", true, "message", "Mercado Pago conectado"));
        } catch (Exception e) {
            if (frontErrorRedirect != null && !frontErrorRedirect.isBlank()) {
                return ResponseEntity.status(302).location(URI.create(frontErrorRedirect)).build();
            }
            return ResponseEntity.badRequest().body(java.util.Map.of("ok", false, "error", e.getMessage()));
        }
    }

    /**
     * Para mostrar estado en Configuración
     */
    @GetMapping("/status")
    public ResponseEntity<MpConnectStatusResponse> status() {
        Long userId = authUtil.extractUserId();
        Usuario u = usuarioRepository.findById(userId).orElseThrow();

        return ResponseEntity.ok(
                new MpConnectStatusResponse(
                        u.isMpConnected(),
                        null,
                        u.getMpTokenExpiresAt()
                )
        );
    }

    /**
     * Desconectar (borra tokens)
     */
    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect() {
        Long userId = authUtil.extractUserId();
        Usuario u = usuarioRepository.findById(userId).orElseThrow();

        u.setMpConnected(false);
        u.setMpAccessToken(null);
        u.setMpRefreshToken(null);
        u.setMpTokenExpiresAt(null);

        usuarioRepository.save(u);

        return ResponseEntity.ok(java.util.Map.of("ok", true, "message", "Mercado Pago desconectado"));
    }
}