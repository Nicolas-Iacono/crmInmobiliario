package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.NotaEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.NotaModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.NotaSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.IImageUrlsService;
import com.backend.crmInmobiliario.service.INotaService;
import com.backend.crmInmobiliario.service.impl.ImagenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "https://tuinmo.net")
@RestController
@RequestMapping("/api/notas")
@RequiredArgsConstructor
public class NotaController {


    private static final Logger LOGGER = LoggerFactory.getLogger(NotaController.class);

    private final INotaService notaService;
    private final ImagenService imagenService;
    private final ObjectMapper objectMapper;

    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getDetails() == null) return null;

        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();

        Object raw = details.get("userId");
        if (raw == null) return null;
        if (raw instanceof Long l) return l;
        if (raw instanceof Integer i) return i.longValue();
        if (raw instanceof String s) return Long.parseLong(s);
        return null;
    }

    // 🔹 1. Listar todas las notas
    @GetMapping("/listar")
    public ResponseEntity<List<NotaSalidaDto>> listarNotas() {
        LOGGER.info("GET /api/notas");
        return ResponseEntity.ok(notaService.listarNotas());
    }

    // 🔹 2. Crear una nota

    @PostMapping("/crear")
    public ResponseEntity<NotaSalidaDto> crearNota(@Valid @RequestBody NotaEntradaDto notaEntradaDto) throws ResourceNotFoundException, IOException {
        LOGGER.info("POST /api/notas");
        System.out.println("DTO recibido: " + notaEntradaDto);
        return ResponseEntity.ok(notaService.crearNota(notaEntradaDto));
    }

    // 🔹 3. Buscar nota por ID
    @GetMapping("/{id}")
    public ResponseEntity<NotaSalidaDto> obtenerNotaPorId(@PathVariable Long id) throws ResourceNotFoundException {
        LOGGER.info("GET /api/notas/{}", id);
        return ResponseEntity.ok(notaService.buscarNotaPorId(id));
    }
    

    // 🔹 5. Modificar estado de una nota
    @PutMapping("/modificar-estado")
    public ResponseEntity<NotaSalidaDto> modificarEstadoNota(@Valid @RequestBody NotaModificacionDto notaModificacionDto) throws ResourceNotFoundException {
        LOGGER.info("PUT /api/notas/modificar-estado");
        return ResponseEntity.ok(notaService.modificarEstado(notaModificacionDto));
    }

    // 🔹 6. Eliminar nota
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNota(@PathVariable Long id) throws ResourceNotFoundException {
        LOGGER.info("DELETE /api/notas/{}", id);
        notaService.eliminarNota(id);
        return ResponseEntity.noContent().build();
    }

    @CrossOrigin(origins = "https://tuinmo.net")
    @PostMapping("/{id}/imagenes")
    public ResponseEntity<?> subirImagenesANota(@PathVariable Long id,
                                                     @RequestParam("files") MultipartFile[] archivos) {
        try {
            List<ImgUrlSalidaDto> imagenes = imagenService.subirImagenesYAsociarANota(id, archivos);
            return ResponseEntity.ok(imagenes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al subir las imágenes: " + e.getMessage());
        }
    }
    @CrossOrigin(origins = "https://tuinmo.net")
    @DeleteMapping("/{idNota}/imagenes/{idImagen}")
    public ResponseEntity<?> eliminarImagen(
            @PathVariable Long idNota,
            @PathVariable Long idImagen) {
        try {
            imagenService.eliminarImagenDeNota(idNota, idImagen);
            return ResponseEntity.ok("Imagen eliminada correctamente.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar la imagen: " + e.getMessage());
        }
    }


    @PostMapping(value = "/crear-con-imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotaSalidaDto> crearConImagenes(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "imagenes", required = false) MultipartFile[] imagenes
    ) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = getUserId(authentication);

        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        NotaEntradaDto dto = objectMapper.readValue(dataJson, NotaEntradaDto.class);

        return ResponseEntity.ok(
                notaService.crearNotaConImagenes(userId, authentication, dto, imagenes)
        );
    }


    @GetMapping("/por-contrato/{contratoId}")
    public ResponseEntity<List<NotaSalidaDto>> obtenerNotasPorContrato(
            @PathVariable Long contratoId,
            Authentication auth
    ) throws ResourceNotFoundException {

        Long userId = getUserId(auth); // tu forma actual (ej: del JWT o principal)

        return ResponseEntity.ok(
                notaService.listarNotasPorContrato(userId, auth, contratoId)
        );
    }

}
