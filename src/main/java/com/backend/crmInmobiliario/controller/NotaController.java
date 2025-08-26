package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.NotaEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.NotaModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.NotaSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.IImageUrlsService;
import com.backend.crmInmobiliario.service.INotaService;
import com.backend.crmInmobiliario.service.impl.ImagenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@CrossOrigin(origins = "https://tuinmo.net")
@RestController
@RequestMapping("/api/notas")
@RequiredArgsConstructor
public class NotaController {


    private static final Logger LOGGER = LoggerFactory.getLogger(NotaController.class);

    private final INotaService notaService;
    private final ImagenService imagenService;
    // 🔹 1. Listar todas las notas
    @GetMapping("/listar")
    public ResponseEntity<List<NotaSalidaDto>> listarNotas() {
        LOGGER.info("GET /api/notas");
        return ResponseEntity.ok(notaService.listarNotas());
    }

    // 🔹 2. Crear una nota

    @PostMapping("/crear")
    public ResponseEntity<NotaSalidaDto> crearNota(@Valid @RequestBody NotaEntradaDto notaEntradaDto) throws ResourceNotFoundException {
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
}
