package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.visita.VisitaEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.visita.VisitaModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.visita.VisitaSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.IVisitaService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import com.backend.crmInmobiliario.utils.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visitas")
@CrossOrigin(origins = "https://tuinmo.net")
public class VisitaController {

    private final IVisitaService visitaService;
    private final AuthUtil authUtil;
    public VisitaController(IVisitaService visitaService, AuthUtil authUtil) {
        this.visitaService = visitaService;
        this.authUtil = authUtil;
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VisitaSalidaDto>> crear(@Valid @RequestBody VisitaEntradaDto dto) {
        try {
            VisitaSalidaDto salida = visitaService.crearVisita(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Visita creada correctamente", salida));
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VisitaSalidaDto>> buscar(@PathVariable Long id) throws ResourceNotFoundException {
        return ResponseEntity.ok(new ApiResponse<>("Visita encontrada", visitaService.buscarVisita(id)));
    }

    @GetMapping("/propiedad/{propiedadId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<VisitaSalidaDto>>> listarPorPropiedad(@PathVariable Long propiedadId) {
        return ResponseEntity.ok(new ApiResponse<>("Visitas de la propiedad", visitaService.listarVisitasPorPropiedad(propiedadId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VisitaSalidaDto>> actualizar(@PathVariable Long id, @RequestBody VisitaModificacionDto dto) {
        try {
            return ResponseEntity.ok(new ApiResponse<>("Visita actualizada", visitaService.actualizarVisita(id, dto)));
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) throws ResourceNotFoundException {
        visitaService.eliminarVisita(id);
        return ResponseEntity.ok(new ApiResponse<>("Visita eliminada", null));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<VisitaSalidaDto>> listarMisVisitas() {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(visitaService.listarVisitasPorUsuarioId(userId));
    }
}
