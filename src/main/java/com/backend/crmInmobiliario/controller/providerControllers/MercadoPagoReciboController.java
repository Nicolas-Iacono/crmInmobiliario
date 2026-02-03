package com.backend.crmInmobiliario.controller.providerControllers;

import com.backend.crmInmobiliario.DTO.salida.inquilino.MpInitPointResponse;
import com.backend.crmInmobiliario.service.impl.mercadoPago.MercadoPagoReciboService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/pagos/mercadopago")
public class MercadoPagoReciboController {
    private final AuthUtil authUtil;
    private final MercadoPagoReciboService mpService;

    public MercadoPagoReciboController(MercadoPagoReciboService mpService,AuthUtil authUtil) {
        this.mpService = mpService;
        this.authUtil = authUtil;
    }

    @PostMapping("/recibos/{reciboId}/preferencia")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> crearPreferencia(@PathVariable Long reciboId) {
        try {
            Long userIdInquilino = authUtil.extractUserId();
            MpInitPointResponse response = mpService.crearLinkPagoRecibo(reciboId, userIdInquilino);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "No se pudo iniciar el pago",
                    "detalle", e.getMessage()
            ));
        }
    }
}
