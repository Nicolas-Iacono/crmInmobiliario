package com.backend.crmInmobiliario.controller.oficios;

import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioCalificacionEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioImagenPerfilEmpresaEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioServicioEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.RegistroOficioProveedorDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioProveedorSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioServicioSalidaDto;
import com.backend.crmInmobiliario.entity.oficios.CategoriaOficio;
import com.backend.crmInmobiliario.service.oficios.IOficioProveedorService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/proveedores/mi-perfil/servicios")
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<List<OficioServicioSalidaDto>> listarMisServicios() {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(oficioProveedorService.listarMisServicios(userId));
    }

    @PutMapping("/proveedores/mi-perfil/servicios/{servicioId}")
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<OficioServicioSalidaDto> editarServicio(
            @PathVariable Long servicioId,
            @RequestBody OficioServicioEntradaDto dto
    ) {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(oficioProveedorService.editarServicio(userId, servicioId, dto));
    }

    @DeleteMapping("/proveedores/mi-perfil/servicios/{servicioId}")
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<Void> eliminarServicio(@PathVariable Long servicioId) {
        Long userId = authUtil.extractUserId();
        oficioProveedorService.eliminarServicio(userId, servicioId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/proveedores/mi-perfil/imagen-perfil")
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<OficioProveedorSalidaDto> actualizarImagenPerfilEmpresa(
            @Valid @RequestBody OficioImagenPerfilEmpresaEntradaDto dto
    ) {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(oficioProveedorService.actualizarImagenPerfilEmpresa(userId, dto));
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
