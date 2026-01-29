package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.prospecto.ProspectoEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.ProspectoModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.PropiedadSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.prospecto.ProspectoSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.impl.ProspectoService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import com.backend.crmInmobiliario.utils.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/prospecto")
@CrossOrigin(origins = "https://tuinmo.net")
public class ProspectoController {
    private final ProspectoService prospectoService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ProspectoSalidaDto>> crearProspecto(
            @RequestBody ProspectoEntradaDto dto,
            Authentication auth) {
        try {
            Long userId = jwtUtil.extractUserIdFromAuth(auth);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>("Usuario no autenticado", null));
            }
            ProspectoSalidaDto salida = prospectoService.crearProspecto(userId, dto);
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
            @RequestBody ProspectoModificacionDto dto,
            Authentication auth) {
        try {
            Long userId = jwtUtil.extractUserIdFromAuth(auth);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>("Usuario no autenticado", null));
            }
            ProspectoSalidaDto salida = prospectoService.actualizarProspecto(userId, id, dto);
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
    @GetMapping("/mis")
    public ResponseEntity<ApiResponse<List<ProspectoSalidaDto>>> listarProspectosPorUsuario(Authentication auth) {
        try {
            Long userId = jwtUtil.extractUserIdFromAuth(auth);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>("Usuario no autenticado", null));
            }
            List<ProspectoSalidaDto> salida = prospectoService.listarProspectosPorUsuario(userId);
            return ResponseEntity.ok(new ApiResponse<>("Prospectos del usuario.", salida));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al listar prospectos", null));
        }
    }

    @Transactional
    @GetMapping("/{id}/propiedades-compatibles")
    public ResponseEntity<ApiResponse<List<PropiedadSalidaDto>>> listarPropiedadesCompatibles(
            @PathVariable Long id,
            Authentication auth) {
        try {
            Long userId = jwtUtil.extractUserIdFromAuth(auth);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>("Usuario no autenticado", null));
            }
            List<PropiedadSalidaDto> salida = prospectoService.listarPropiedadesCompatibles(userId, id);
            return ResponseEntity.ok(new ApiResponse<>("Propiedades compatibles.", salida));
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
            @PathVariable Long propiedadId,
            Authentication auth) {
        try {
            Long userId = jwtUtil.extractUserIdFromAuth(auth);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>("Usuario no autenticado", null));
            }
            prospectoService.notificarPropiedadCompatible(userId, id, propiedadId);
            return ResponseEntity.ok(new ApiResponse<>("Notificación enviada correctamente.", null));
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al enviar la notificación", null));
        }
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> eliminarProspecto(@PathVariable Long id, Authentication auth) {
        try {
            Long userId = jwtUtil.extractUserIdFromAuth(auth);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>("Usuario no autenticado", null));
            }
            prospectoService.eliminarProspecto(userId, id);
            return ResponseEntity.ok(new ApiResponse<>("Prospecto eliminado correctamente.", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al eliminar el prospecto", null));
        }
    }
}
