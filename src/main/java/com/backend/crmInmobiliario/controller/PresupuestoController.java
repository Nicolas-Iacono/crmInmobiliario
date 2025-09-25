package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.PresupuestoEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.PresupuestoSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.IPresupuestoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presupuestos")
@RequiredArgsConstructor
public class PresupuestoController {

    private final IPresupuestoService presupuestoService;

    @PostMapping
    public ResponseEntity<PresupuestoSalidaDto> crear(@RequestBody PresupuestoEntradaDto dto) throws ResourceNotFoundException {
        return ResponseEntity.ok(presupuestoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PresupuestoSalidaDto> actualizar(@PathVariable Long id,
                                                           @RequestBody PresupuestoEntradaDto dto) throws ResourceNotFoundException {
        return ResponseEntity.ok(presupuestoService.actualizar(id, dto));
    }

    // ✅ Buscar por ID (solo números)
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        PresupuestoSalidaDto dto = presupuestoService.buscarPorId(id);
        return ResponseEntity.ok(dto);
    }

    // ✅ Listar por username (ruta explícita)
    @PreAuthorize("permitAll()")
    @Transactional
    @GetMapping("/usuario/{username}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable String username) {
        return ResponseEntity.ok(presupuestoService.listarPorUsuario(username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) throws ResourceNotFoundException {
        presupuestoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
