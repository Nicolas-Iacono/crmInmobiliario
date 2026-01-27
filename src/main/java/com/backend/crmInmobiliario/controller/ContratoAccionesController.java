package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.renovaciones.RenovarContratoRequest;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.service.impl.ContratoAccionesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/acciones")
@RequiredArgsConstructor
public class ContratoAccionesController {
    private final ContratoAccionesService service;

    @PostMapping("/{id}/finalizar")
    public void finalizar(@PathVariable Long id) {
        service.finalizar(id);
    }

    @PostMapping("/{id}/renovar")
    public ContratoSalidaDto renovar(@PathVariable Long id, @RequestBody RenovarContratoRequest req) {
        return service.renovarContrato(id, req);
    }
}
