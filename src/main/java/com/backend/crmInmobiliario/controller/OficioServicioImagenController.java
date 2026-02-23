package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.salida.aliados.OficioProveedorSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.aliados.OficioServicioSalidaDto;
import com.backend.crmInmobiliario.service.impl.aliados.OficioImagenService;
import com.backend.crmInmobiliario.service.impl.aliados.OficioProveedorServiceImpl;
import com.backend.crmInmobiliario.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/oficios/servicios")
@CrossOrigin(origins = "https://tuinmo.net")
@RequiredArgsConstructor
public class OficioServicioImagenController {

    private final OficioImagenService oficioImagenService;
    private final AuthUtil authUtil;
    private final OficioProveedorServiceImpl oficioProveedorService;

    // POST /servicios/{id}/imagenes (agregar nuevas)
    @PostMapping(value = "/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<OficioServicioSalidaDto> agregarImagenes(
            @RequestPart("imagenes") MultipartFile[] imagenes
    ) {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(
                oficioImagenService.agregarNuevasPorUsuario(userId, imagenes)
        );
    }

    // PUT /servicios/{id}/imagenes (replace total)
    @PutMapping(value = "/{id}/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<OficioServicioSalidaDto> reemplazarImagenes(
            @PathVariable Long id,
            @RequestPart(value = "imagenes", required = false) MultipartFile[] imagenes
    ) {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(oficioImagenService.reemplazarTodas(userId, id, imagenes));
    }

    // DELETE /servicios/{id}/imagenes/{imageId} (borrar una)
    @DeleteMapping("/{id}/imagenes/{imageId}")
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<OficioServicioSalidaDto> borrarUna(
            @PathVariable Long id,
            @PathVariable Long imageId
    ) {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(oficioImagenService.eliminarUna(userId, id, imageId));
    }


    @PutMapping(value = "/proveedores/me/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OficioProveedorSalidaDto> reemplazarFotoPerfil(
            @RequestPart("imagen") MultipartFile imagen
    ) throws IOException {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(oficioProveedorService.reemplazarFotoPerfil(userId, imagen));
    }

    // DELETE /api/oficios/proveedores/me/imagen
    @DeleteMapping("/proveedores/me/imagen")
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<Void> borrarFotoPerfil() {
        Long userId = authUtil.extractUserId();
        oficioProveedorService.eliminarFotoPerfil(userId);
        return ResponseEntity.noContent().build();
    }
}

