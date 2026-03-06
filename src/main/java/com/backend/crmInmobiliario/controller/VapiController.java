package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.vapi.VapiOutboundCallRequest;
import com.backend.crmInmobiliario.service.impl.vapi.VapiCallService;
import com.backend.crmInmobiliario.service.impl.vapi.VapiLeadService;
import com.backend.crmInmobiliario.utils.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/vapi")
@RequiredArgsConstructor
public class VapiController {

    private final VapiLeadService vapiLeadService;
    private final VapiCallService vapiCallService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<Void> receiveWebhook(@RequestBody JsonNode payload) {
        try {
            vapiLeadService.processWebhook(payload);
        } catch (Exception e) {
            log.error("Error procesando webhook de Vapi", e);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/calls")
    public ResponseEntity<JsonNode> startCall(@Valid @RequestBody VapiOutboundCallRequest request,
                                              Authentication authentication) {
        Long userId = jwtUtil.extractUserIdFromAuth(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        JsonNode response = vapiCallService.startOutboundCall(userId, request);
        return ResponseEntity.ok(response);
    }
}
