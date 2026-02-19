package com.backend.crmInmobiliario.controller.oficios;

import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioCalificacionEntradaDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioProveedorCreateDto;
import com.backend.crmInmobiliario.DTO.entrada.oficios.OficioServicioCreateDto;
import com.backend.crmInmobiliario.DTO.modificacion.OficioProveedorUpdateDto;
import com.backend.crmInmobiliario.DTO.modificacion.OficioServicioUpdateDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioProveedorSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.oficios.OficioServicioSalidaDto;
import com.backend.crmInmobiliario.entity.oficios.CategoriaOficio;
import com.backend.crmInmobiliario.service.oficios.IOficioProveedorService;
import com.backend.crmInmobiliario.utils.AuthUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/oficios")
@CrossOrigin(origins = "https://tuinmo.net")
@PreAuthorize("denyAll()")
public class OficioProveedorController {

    private final IOficioProveedorService oficioProveedorService;
    private final AuthUtil authUtil;
    private final ObjectMapper objectMapper;

    public OficioProveedorController(IOficioProveedorService oficioProveedorService,
                                     AuthUtil authUtil,
                                     ObjectMapper objectMapper) {
        this.oficioProveedorService = oficioProveedorService;
        this.authUtil = authUtil;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/categorias")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<CategoriaOficio>> listarCategorias() {
        return ResponseEntity.ok(oficioProveedorService.listarCategorias());
    }

    @PostMapping("/proveedores/registro")
    @PreAuthorize("permitAll()")
    public ResponseEntity<OficioProveedorSalidaDto> registrarProveedor(@Valid @RequestBody OficioProveedorCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(oficioProveedorService.registrarProveedor(dto));
    }

    @GetMapping("/proveedores")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OficioProveedorSalidaDto>> listarProveedoresVisibles() {
        return ResponseEntity.ok(oficioProveedorService.listarProveedoresVisibles());
    }

    @GetMapping("/proveedores/mi-perfil")
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<OficioProveedorSalidaDto> obtenerMiPerfil() {
        return ResponseEntity.ok(oficioProveedorService.obtenerMiPerfil(authUtil.extractUserId()));
    }

    @PutMapping("/proveedores/mi-perfil")
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<OficioProveedorSalidaDto> actualizarMiPerfil(@Valid @RequestBody OficioProveedorUpdateDto dto) {
        return ResponseEntity.ok(oficioProveedorService.actualizarMiPerfil(authUtil.extractUserId(), dto));
    }

    @DeleteMapping("/proveedores/mi-perfil")
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<Void> eliminarMiPerfil() {
        oficioProveedorService.eliminarMiPerfil(authUtil.extractUserId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/proveedores/mi-perfil/imagen-perfil", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<OficioProveedorSalidaDto> actualizarImagenPerfilEmpresa(@RequestPart("imagen") MultipartFile archivo)
            throws IOException {
        return ResponseEntity.ok(oficioProveedorService.actualizarImagenPerfil(authUtil.extractUserId(), archivo));
    }

    @PostMapping(value = "/proveedores/mi-perfil/servicios", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<OficioServicioSalidaDto> crearServicio(@RequestPart("data") String dataJson,
                                                                 @RequestPart(value = "imagenes", required = false) MultipartFile[] imagenes)
            throws IOException {
        OficioServicioCreateDto dto = objectMapper.readValue(dataJson, OficioServicioCreateDto.class);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(oficioProveedorService.agregarServicio(authUtil.extractUserId(), dto, imagenes));
    }

    @GetMapping("/proveedores/mi-perfil/servicios")
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<List<OficioServicioSalidaDto>> listarMisServicios() {
        return ResponseEntity.ok(oficioProveedorService.listarMisServicios(authUtil.extractUserId()));
    }

    @PutMapping(value = "/proveedores/mi-perfil/servicios/{servicioId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<OficioServicioSalidaDto> editarServicio(@PathVariable Long servicioId,
                                                                  @RequestPart("data") String dataJson,
                                                                  @RequestPart(value = "imagenes", required = false) MultipartFile[] imagenes)
            throws IOException {
        OficioServicioUpdateDto dto = objectMapper.readValue(dataJson, OficioServicioUpdateDto.class);
        return ResponseEntity.ok(oficioProveedorService.editarServicio(authUtil.extractUserId(), servicioId, dto, imagenes));
    }

    @DeleteMapping("/proveedores/mi-perfil/servicios/{servicioId}")
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<Void> eliminarServicio(@PathVariable Long servicioId) {
        oficioProveedorService.eliminarServicio(authUtil.extractUserId(), servicioId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/proveedores/mi-perfil/plan")
    @PreAuthorize("hasRole('OFICIO_ADMIN')")
    public ResponseEntity<OficioProveedorSalidaDto> asignarPlan(@RequestParam Long planId) {
        return ResponseEntity.ok(oficioProveedorService.asignarPlan(authUtil.extractUserId(), planId));
    }

    @PostMapping("/proveedores/{proveedorId}/calificaciones")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<OficioProveedorSalidaDto> calificarProveedor(@PathVariable Long proveedorId,
                                                                       @Valid @RequestBody OficioCalificacionEntradaDto dto) {
        return ResponseEntity.ok(oficioProveedorService.calificarProveedor(proveedorId, authUtil.extractUserId(), dto));
    }
}
