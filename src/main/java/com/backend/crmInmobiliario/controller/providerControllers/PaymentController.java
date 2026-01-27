package com.backend.crmInmobiliario.controller.providerControllers;

import com.backend.crmInmobiliario.DTO.salida.planesYSuscripcion.PaymentSalidaDto;
import com.backend.crmInmobiliario.service.impl.mercadoPago.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
private final PaymentService paymentService;
    @GetMapping("/by-user/{username}")
    public ResponseEntity<List<PaymentSalidaDto>> listarPagosPorUsuario(@PathVariable String username) {
        List<PaymentSalidaDto> pagos = paymentService.obtenerPagosPorUsuario(username);
        return ResponseEntity.ok(pagos);
    }




}
