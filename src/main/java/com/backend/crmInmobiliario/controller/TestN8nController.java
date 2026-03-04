package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.N8N.AysaWebhookRequest;
import com.backend.crmInmobiliario.DTO.salida.N8N.AysaScrapeResponse;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.ContratoRepository;
import com.backend.crmInmobiliario.service.impl.n8n.N8nAysaService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/n8n")
@RequiredArgsConstructor
public class TestN8nController {

    private final N8nAysaService n8nAysaService;
    private final AuthUtil authUtil;
    private final ContratoRepository contratoRepository;

    @PostMapping("/{id}/servicios/aysa/sync")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AysaScrapeResponse> syncAysa(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String auth
    ) throws ResourceNotFoundException {

        // 1) Seguridad: contrato del usuario logueado
        Long userId = authUtil.extractUserId();

        Contrato contrato = contratoRepository.findByIdAndUsuarioId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato no encontrado"));

        // 2) Datos necesarios para AySA
        String cuenta = contrato.getAguaCuentaServicio();
        if (cuenta == null || cuenta.isBlank()) {
            throw new IllegalArgumentException("El contrato no tiene aguaCuentaServicio cargada");
        }

        String email = contrato.getUsuario().getEmail();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El usuario no tiene email");
        }

        // 3) Armar request a n8n
        AysaWebhookRequest req = new AysaWebhookRequest();
        req.setRequestId(UUID.randomUUID().toString());
        req.setCuentaServicios(cuenta);
        req.setEmail(email);
        req.setContratoId(contrato.getId());

        // 4) Llamar n8n
        // Si auth viene null, igual podés mandarlo sin header
        AysaScrapeResponse res = n8nAysaService.llamarWebhookAysa(req, auth);

        // 5) Devolver respuesta
        return ResponseEntity.ok(res);
    }

}
