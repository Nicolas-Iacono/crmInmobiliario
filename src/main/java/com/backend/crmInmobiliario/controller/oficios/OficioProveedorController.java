package com.backend.crmInmobiliario.controller.oficios;

import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioCalificacionEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioServicioEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.RegistroOficioProveedorDto;
import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioProveedorSalidaDto;
import com.backend.crmInmobiliario.entity.oficios.CategoriaOficio;
import com.backend.crmInmobiliario.service.oficios.IOficioProveedorService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/oficios")
@CrossOrigin(origins = "https://tuinmo.net")
@PreAuthorize("denyAll()")
public class OficioProveedorController {

    private final IOficioProveedorService oficioProveedorService;
    private final AuthUtil authUtil;

    public OficioProveedorController(IOficioProveedorService oficioProveedorService, AuthUtil authUtil) {
        this.oficioProveedorService = oficioProveedorService;
        this.authUtil = authUtil;
    }

    @GetMapping("/categorias")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<CategoriaOficio>> listarCategorias() {
        return ResponseEntity.ok(oficioProveedorService.listarCategorias());
    }

    @PostMapping("/proveedores/registro")
    @PreAuthorize("permitAll()")
    public ResponseEntity<OficioProveedorSalidaDto> registrarProveedor(@Valid @RequestBody RegistroOficioProveedorDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(oficioProveedorService.registrarProveedor(dto));
    }

    @GetMapping("/proveedores")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OficioProveedorSalidaDto>> listarProveedoresVisibles() {
        return ResponseEntity.ok(oficioProveedorService.listarProveedoresVisibles());
    }

    @PostMapping("/proveedores/mi-perfil/servicios")
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<OficioProveedorSalidaDto> crearServicio(@RequestBody OficioServicioEntradaDto dto) {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(oficioProveedorService.agregarServicio(userId, dto));
    }

    @PostMapping("/proveedores/mi-perfil/logo")
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<ImgUrlSalidaDto> subirLogoProveedor(@RequestParam("file") MultipartFile archivo) {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(oficioProveedorService.subirLogoProveedor(userId, archivo));
    }

    @PostMapping("/proveedores/mi-perfil/imagenes-empresa")
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<OficioProveedorSalidaDto> subirImagenesEmpresa(@RequestParam("files") MultipartFile[] archivos) {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(oficioProveedorService.subirImagenesEmpresa(userId, archivos));
    }

    @PostMapping("/proveedores/mi-perfil/servicios/{servicioId}/imagenes")
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<OficioProveedorSalidaDto> subirImagenesTrabajo(
            @PathVariable Long servicioId,
            @RequestParam("files") MultipartFile[] archivos
    ) {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(oficioProveedorService.subirImagenesTrabajo(userId, servicioId, archivos));
    }

    @PutMapping("/proveedores/mi-perfil/plan")
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<OficioProveedorSalidaDto> asignarPlan(@RequestParam Long planId) {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(oficioProveedorService.asignarPlan(userId, planId));
    }

    @PostMapping("/proveedores/{proveedorId}/calificaciones")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<OficioProveedorSalidaDto> calificarProveedor(
            @PathVariable Long proveedorId,
            @Valid @RequestBody OficioCalificacionEntradaDto dto
    ) {
        Long inmobiliariaId = authUtil.extractUserId();
        return ResponseEntity.ok(oficioProveedorService.calificarProveedor(proveedorId, inmobiliariaId, dto));
    }
}
