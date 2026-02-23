package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.aliados.ResenaCrearDto;
import com.backend.crmInmobiliario.DTO.salida.aliados.OficioProveedorCardDto;
import com.backend.crmInmobiliario.DTO.salida.aliados.OficioProveedorDetalleDto;
import com.backend.crmInmobiliario.DTO.salida.aliados.OficioProveedorSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.aliados.OficioServicioPublicoDto;
import com.backend.crmInmobiliario.service.impl.aliados.OficioMarketplaceService;
import com.backend.crmInmobiliario.service.impl.aliados.OficioResenaService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/marketplace")
public class OficioMarketplaceController {

    private final OficioMarketplaceService marketplaceService;
    private final OficioResenaService resenaService;
    private final AuthUtil authUtil;

    public OficioMarketplaceController(OficioMarketplaceService marketplaceService, OficioResenaService resenaService, AuthUtil authUtil) {
        this.marketplaceService = marketplaceService;
        this.resenaService = resenaService;
        this.authUtil = authUtil;
    }
    // 1) Proveedores (lista)
    @GetMapping("/proveedores")
    @PreAuthorize("isAuthenticated()") // o el rol de inmobiliaria
    public ResponseEntity<List<OficioProveedorCardDto>> listarProveedores() {
        return ResponseEntity.ok(marketplaceService.listarProveedores());
    }

    // 2) Detalle proveedor (incluye reseñas)
    @GetMapping("/proveedores/{proveedorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OficioProveedorDetalleDto> obtenerDetalleProveedor(
            @PathVariable("proveedorId") Long proveedorId
    ) {
        return ResponseEntity.ok(marketplaceService.obtenerDetalle(proveedorId));
    }

    // 3) Servicios públicos de un proveedor
    @GetMapping("/proveedores/{proveedorId}/servicios")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OficioServicioPublicoDto>> serviciosDeProveedor(
            @PathVariable("proveedorId") Long proveedorId
    ) {
        return ResponseEntity.ok(marketplaceService.listarServiciosDeProveedor(proveedorId));
    }

    // 4) Crear o actualizar reseña (inmobiliaria)
    @PostMapping("/proveedores/{proveedorId}/resenas")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')") // poné el rol real de "inmobiliaria"
    public ResponseEntity<Void> crearOActualizarResena(
            @PathVariable("proveedorId") Long proveedorId,
            @RequestBody ResenaCrearDto dto
    ) {
        Long userId = authUtil.extractUserId();
        resenaService.crearOActualizar(userId, proveedorId, dto);
        return ResponseEntity.ok().build();
    }

    // 5) Borrar mi reseña
    @DeleteMapping("/proveedores/{proveedorId}/resenas/me")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> borrarMiResena(
            @PathVariable("proveedorId") Long proveedorId
    ) {
        Long userId = authUtil.extractUserId();
        resenaService.eliminarMiResena(userId, proveedorId);
        return ResponseEntity.noContent().build();
    }
}

