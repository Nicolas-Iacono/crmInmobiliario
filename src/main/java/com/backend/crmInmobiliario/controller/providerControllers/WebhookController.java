package com.backend.crmInmobiliario.controller.providerControllers;


import com.backend.crmInmobiliario.service.impl.mercadoPago.WebhookService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoint para recibir las notificaciones Webhook de Mercado Pago.
 * URL configurada: /api/webhooks/mercadopago
 */
@RestController
@RequestMapping("/api/webhooks/mercadopago")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @PostMapping
    public ResponseEntity<Void> receiveWebhook(
            @RequestBody(required = false) Map<String, Object> payload,
            @RequestParam(name = "data.id", required = false) String dataIdParam,
            @RequestParam(name = "type", required = false) String typeParam,
            @RequestHeader Map<String, String> headers) {

        // ---------------------------------------------------------
        // 🔹 LOGS DETALLADOS DEL WEBHOOK RECIBIDO
        // ---------------------------------------------------------
        logger.info("📬 Webhook recibido de Mercado Pago");
        logger.info("─────────────────────────────────────────────");
        logger.info("🔸 Type param: {}", typeParam);
        logger.info("🔸 DataId param: {}", dataIdParam);

        if (payload != null) {
            logger.info("🔹 Payload keys: {}", payload.keySet());
            logger.info("🔹 Payload completo: {}", payload);
        } else {
            logger.info("🔹 Payload: <VACÍO>");
        }

        logger.info("🔹 Headers: {}", headers);
        logger.info("─────────────────────────────────────────────");

        // Nota: La notificación puede venir con el ID en el cuerpo o en los query params.
        String type = typeParam;
        String dataId = dataIdParam;

        if (payload != null && payload.containsKey("type")) {
            type = (String) payload.get("type");
        }
        if (payload != null && payload.containsKey("data")) {
            // El 'data.id' puede estar anidado en el body para algunos eventos.
            Map<String, String> data = (Map<String, String>) payload.get("data");
            if (data != null && data.containsKey("id")) {
                dataId = data.get("id");
            }
        }

        // 1. Validaciones básicas antes de procesar
        if (type == null || dataId == null) {
            logger.warn("Webhook recibido sin tipo o ID de datos: Type={}, DataId={}", type, dataId);
            return ResponseEntity.badRequest().build();
        }
        // ---------------------------------------------------------
        // 🔹 Log de confirmación final
        // ---------------------------------------------------------
        logger.info("✅ Webhook listo para procesar → Type={}, DataId={}", type, dataId);

        // --- REQUISITO 2A: DEVOLVER 200 OK INMEDIATAMENTE ---
        // Llamamos al servicio de manera asíncrona para liberar el hilo del request
        // y responder el 200 OK dentro del plazo de 22 segundos de MP.
        webhookService.handleNotificationAsync(type, dataId);

        // Retornamos 200 OK (CREATED) inmediatamente, cumpliendo con la exigencia de MP.
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

