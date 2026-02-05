package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.salida.recibo.ReciboAlertaDto;
import com.backend.crmInmobiliario.service.impl.ReciboAlertaService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import com.backend.crmInmobiliario.utils.AuthUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alertas/recibos")
public class ReciboAlertaController {
    private final ReciboAlertaService reciboAlertaService;
    private final AuthUtil authUtil;

    public ReciboAlertaController(ReciboAlertaService reciboAlertaService, AuthUtil authUtil) {
        this.reciboAlertaService = reciboAlertaService;
        this.authUtil = authUtil;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ReciboAlertaDto>>> listarAlertas() {
        try {
            Long usuarioId = authUtil.extractUserId();
            List<ReciboAlertaDto> alertas = reciboAlertaService.listarAlertas(usuarioId);
            return ResponseEntity.ok(new ApiResponse<>("alertas_recibos", alertas));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }

    @PutMapping("/{id}/visto")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReciboAlertaDto>> marcarVisto(@PathVariable Long id) {
        try {
            Long usuarioId = authUtil.extractUserId();
            ReciboAlertaDto alerta = reciboAlertaService.marcarVisto(id, usuarioId);
            return ResponseEntity.ok(new ApiResponse<>("alerta_actualizada", alerta));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }

    @PutMapping("/{id}/no-mostrar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReciboAlertaDto>> marcarNoMostrar(@PathVariable Long id) {
        try {
            Long usuarioId = authUtil.extractUserId();
            ReciboAlertaDto alerta = reciboAlertaService.marcarNoMostrar(id, usuarioId);
            return ResponseEntity.ok(new ApiResponse<>("alerta_actualizada", alerta));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }
}
