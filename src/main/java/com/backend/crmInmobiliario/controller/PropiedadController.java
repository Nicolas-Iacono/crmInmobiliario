package com.backend.crmInmobiliario.controller;


import com.backend.crmInmobiliario.DTO.entrada.propiedades.PropiedadEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.PropiedadModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.ImgUrlSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.PropiedadSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.PropiedadSoloSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.prospecto.ProspectoSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.repository.PropietarioRepository;
import com.backend.crmInmobiliario.service.impl.ImagenService;
import com.backend.crmInmobiliario.service.impl.PropiedadService;
import com.backend.crmInmobiliario.service.impl.ProspectoService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import com.backend.crmInmobiliario.utils.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@AllArgsConstructor
@RequestMapping("/api/propiedad")
@CrossOrigin(origins = "https://tuinmo.net")
public class PropiedadController {
    private final PropiedadService propiedadService;
    private final ImagenService imagenService;
    private final PropietarioRepository propietarioRepository;
    private final AuthUtil authUtil;
    private final ProspectoService prospectoService;

    @Transactional
    @GetMapping("/enum/{username}")
    public Integer enumerar(@PathVariable String username) {
        Integer total = propiedadService.enumerarPropiedades(username);
        return total;
    }


    @PostMapping("/create")
    public ResponseEntity<?> crearPropiedad(@RequestBody PropiedadEntradaDto dto, HttpServletRequest request) {
        PropiedadSalidaDto salida = propiedadService.crearPropiedad(dto, request);
        return ResponseEntity.ok(salida);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PropiedadSalidaDto>> actualizarPropiedad(
            @PathVariable Long id,
            @RequestBody PropiedadModificacionDto dto) {
        try {
            PropiedadSalidaDto actualizado = propiedadService.actualizarPropiedad(id, dto);
            return ResponseEntity.ok(new ApiResponse<>("Propiedad actualizada correctamente.", actualizado));
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al actualizar la propiedad", null));
        }
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<ApiResponse<PropiedadSalidaDto>> buscarPropiedadPorId(@PathVariable Long id){
        try{
            PropiedadSalidaDto propiedadBuscada = propiedadService.buscarPropiedadPorId(id);
            return  ResponseEntity.ok(new ApiResponse<>("Propiedad encontrada, ", propiedadBuscada));
        }catch (ResourceNotFoundException  e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("No se encontro la propiedad buscada, ", null));
        }
    }

    @PutMapping("/propiedad/{id}/asignar-propietario/{propietarioId}")
    public ResponseEntity<?> asignarPropietario(@PathVariable Long id, @PathVariable Long propietarioId) {
        return ResponseEntity.ok(propiedadService.asignarPropietario(id, propietarioId));
    }

    @PutMapping("/propiedad/{id}/quitar-propietario")
    public ResponseEntity<?> quitarPropietario(@PathVariable Long id) {
        return ResponseEntity.ok(propiedadService.quitarPropietario(id));
    }
    @CrossOrigin(origins = "https://tuinmo.net")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PropiedadSoloSalidaDto>>> allPropiedades(){
        List<PropiedadSoloSalidaDto> propiedadesSalidaDtos = propiedadService.listarPropiedades();
        ApiResponse<List<PropiedadSoloSalidaDto>> response =
                new ApiResponse<>("Lista de propietarios: ", propiedadesSalidaDtos);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @CrossOrigin(origins = "https://tuinmo.net")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<?>> eliminarPropiedad(@PathVariable Long id){
        try{
            propiedadService.eliminarPropiedad(id);
            return  ResponseEntity.ok(new ApiResponse<>("Propiedad con ID: " + id + " eliminada.", null ));
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Propiedad no encontrada con el ID: " + id, null));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al eliminar la propiedad", null));
        }
    }

    @GetMapping("/{username}")
    @CrossOrigin(origins = "https://tuinmo.net")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<PropiedadSalidaDto>> getPropiedadByUsername(@PathVariable String username) {
        List<PropiedadSalidaDto> propiedades =propiedadService.buscarPropiedadesPorUsuario(username);
        return ResponseEntity.ok(propiedades);
    }

    @GetMapping("/generar-embeddings")
    public ResponseEntity<?> generarEmbeddings() {
        try {
            Long userId = authUtil.extractUserId();
            propiedadService.generarEmbeddingsParaUsuario(userId);
            return ResponseEntity.ok("✅ Embeddings generados correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }


    @CrossOrigin(origins = "https://tuinmo.net")
    @PostMapping("/{id}/imagenes")
    public ResponseEntity<?> subirImagenesAPropiedad(@PathVariable Long id,
                                                     @RequestParam("files") MultipartFile[] archivos) {
        try {
            List<ImgUrlSalidaDto> imagenes = imagenService.subirImagenesYAsociarAPropiedad(id, archivos);
            return ResponseEntity.ok(imagenes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al subir las imágenes: " + e.getMessage());
        }
    }
    @CrossOrigin(origins = "https://tuinmo.net")
    @DeleteMapping("/{idPropiedad}/imagenes/{idImagen}")
    public ResponseEntity<?> eliminarImagen(
            @PathVariable Long idPropiedad,
            @PathVariable Long idImagen) {
        try {
            imagenService.eliminarImagenDePropiedad(idPropiedad, idImagen);
            return ResponseEntity.ok("Imagen eliminada correctamente.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar la imagen: " + e.getMessage());
        }
    }
    @CrossOrigin(origins = "https://tuinmo.net")
    @GetMapping("/por-propietario")
    public ResponseEntity<List<PropiedadSoloSalidaDto>> obtenerMisPropiedades(Authentication auth) {
        String email = auth.getName(); // viene del JWT
        List<PropiedadSoloSalidaDto> propiedades = propiedadService.buscarPorEmailPropietario(email);
        return ResponseEntity.ok(propiedades);
    }

    @GetMapping("/por-propietario/con-imagenes")
    public ResponseEntity<List<PropiedadSalidaDto>> obtenerMisPropiedadesConImagenes(Authentication auth) {
        String email = auth.getName();
        var propietario = propietarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Propietario no encontrado"));

        List<PropiedadSalidaDto> propiedades = propiedadService.buscarPorPropietarioConImagenes(propietario.getId());
        return ResponseEntity.ok(propiedades);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PropiedadSalidaDto>> listarMisPropiedades() {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(propiedadService.listarPropiedadesPorUsuarioId(userId));
    }
    @GetMapping("/{id}/prospectos-compatibles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ProspectoSalidaDto>>> listarProspectosCompatibles(@PathVariable Long id) {
        try {
            Long userId = authUtil.extractUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>("Usuario no autenticado", null));
            }
            List<ProspectoSalidaDto> salida = prospectoService.listarProspectosCompatibles(id, userId);
            return ResponseEntity.ok(new ApiResponse<>("Prospectos compatibles con la propiedad.", salida));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al listar prospectos compatibles", null));
        }
    }
}
