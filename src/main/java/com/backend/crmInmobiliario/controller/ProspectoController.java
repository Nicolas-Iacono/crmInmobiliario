package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.prospecto.ProspectoEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.ProspectoDisponibilidadDto;
import com.backend.crmInmobiliario.DTO.modificacion.ProspectoModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.PropiedadSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.prospecto.ProspectoSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.impl.ProspectoService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import com.backend.crmInmobiliario.utils.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prospecto")
@CrossOrigin(origins = "https://tuinmo.net")
public class ProspectoController {
    private final ProspectoService prospectoService;
    private final AuthUtil authUtil;

    public ProspectoController(ProspectoService prospectoService, AuthUtil authUtil) {
        this.prospectoService = prospectoService;
        this.authUtil = authUtil;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ProspectoSalidaDto>> crearProspecto(
            @RequestBody ProspectoEntradaDto dto,
            Authentication auth) {
        try {
            Long userId = authUtil.extractUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>("Usuario no autenticado", null));
            }
            ProspectoSalidaDto salida = prospectoService.crearProspecto(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Prospecto creado correctamente.", salida));
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al crear el prospecto", null));
        }
    }


    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProspectoSalidaDto>> actualizarProspecto(
            @PathVariable Long id,
            @RequestBody ProspectoModificacionDto dto) {
        try {
            ProspectoSalidaDto salida = prospectoService.actualizarProspecto(id, dto);
            return ResponseEntity.ok(new ApiResponse<>("Prospecto actualizado correctamente.", salida));
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al actualizar el prospecto", null));
        }
    }

    @Transactional
    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<ApiResponse<ProspectoSalidaDto>> actualizarDisponibilidad(
            @PathVariable Long id,
            @RequestBody ProspectoDisponibilidadDto dto) {
        try {
            ProspectoSalidaDto salida = prospectoService.actualizarDisponibilidad(id, dto.getDisponible());
            return ResponseEntity.ok(new ApiResponse<>("Disponibilidad actualizada correctamente.", salida));
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al actualizar la disponibilidad", null));
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ProspectoSalidaDto>>> listarMisProspectos() {
        List<ProspectoSalidaDto> salida = prospectoService.listarMisProspectos();
        return ResponseEntity.ok(new ApiResponse<>("Prospectos del usuario.", salida));
    }

    @GetMapping("/compatibles/{propiedadId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ProspectoSalidaDto>>> listarProspectosCompatibles(
            @PathVariable Long propiedadId) {
        try {
            Long userId = authUtil.extractUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>("Usuario no autenticado", null));
            }
            List<ProspectoSalidaDto> salida = prospectoService.listarProspectosCompatibles(propiedadId, userId);
            return ResponseEntity.ok(new ApiResponse<>("Prospectos compatibles con la propiedad.", salida));
        } catch (ResourceNotFoundException | AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al listar prospectos compatibles", null));
        }
    }


    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> eliminarProspecto(@PathVariable Long id) {
        try {
            prospectoService.eliminarProspecto(id);
            return ResponseEntity.ok(new ApiResponse<>("Prospecto eliminado correctamente.", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al eliminar el prospecto", null));
        }
    }


    @GetMapping("/{id}/propiedades-compatibles")
    public ResponseEntity<ApiResponse<List<PropiedadSalidaDto>>> listarPropiedadesCompatibles(
            @PathVariable Long id) {
        try {
            Long userId = authUtil.extractUserId();

            List<PropiedadSalidaDto> salida =
                    prospectoService.listarPropiedadesCompatibles(userId, id);

            return ResponseEntity.ok(new ApiResponse<>("Propiedades compatibles.", salida));

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("Usuario no autenticado", null));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(e.getMessage(), null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al listar propiedades compatibles", null));
        }
    }
    @Transactional
    @PostMapping("/{id}/notificar-propiedad/{propiedadId}")
    public ResponseEntity<ApiResponse<?>> notificarPropiedadCompatible(
            @PathVariable Long id,
            @PathVariable Long propiedadId) {
        try {
            Long userId = authUtil.extractUserId();

            prospectoService.notificarPropiedadCompatible(userId, id, propiedadId);

            return ResponseEntity.ok(
                    new ApiResponse<>("Notificación enviada correctamente.", null)
            );

        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al enviar la notificación", null));
        }
    }
}
