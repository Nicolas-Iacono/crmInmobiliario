package com.backend.crmInmobiliario.controller;


import com.backend.crmInmobiliario.DTO.salida.aliados.OficioProveedorSalidaDto;
import com.backend.crmInmobiliario.service.IOficioProveedorService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin(origins = "https://tuinmo.net")
@RequiredArgsConstructor
public class OficioProveedorController {

    private final IOficioProveedorService proveedorService;
    private final AuthUtil authUtil;

    @GetMapping("/me")
    public ResponseEntity<OficioProveedorSalidaDto> me() {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(proveedorService.obtenerMiPerfil(userId));
    }
}

