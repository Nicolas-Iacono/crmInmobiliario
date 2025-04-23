package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.PropietarioEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.propietario.PropietarioSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.impl.ImagenService;
import com.backend.crmInmobiliario.service.impl.PropietarioService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador para la entidad Propietario
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/propietario")
@CrossOrigin(origins = "https://saddlebrown-coyote-218911.hostingersite.com")
public class PropietarioController {

    private final PropietarioService propietarioService;
    private final ImagenService imagenService;
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PropietarioSalidaDto>> crearPropietario(@Valid @RequestBody PropietarioEntradaDto PropietarioEntradaDto) {
        try {
            PropietarioSalidaDto propietarioSalidaDto = propietarioService.crearPropietario(PropietarioEntradaDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Propietario creado correctamente.", propietarioSalidaDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("El idCategory o idTheme no se encuentra en la DB", null));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PropietarioSalidaDto>>> allPropietarios(){
        List<PropietarioSalidaDto> propietarioSalidaDtos = propietarioService.listarPropietarios();
        ApiResponse<List<PropietarioSalidaDto>> response =
                new ApiResponse<>("Lista de propietarios: ", propietarioSalidaDtos);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<?>> eliminarPropietario(@PathVariable Long id){
        try{
            propietarioService.eliminarPropietario(id);
            return  ResponseEntity.ok(new ApiResponse<>("Propietario con ID: " + id + " eliminado.", null ));
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Propietario no encontrado con el ID: " + id, null));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al eliminar el propietario", null));
        }
    }

    @GetMapping("/{username}")
    @CrossOrigin(origins = "https://saddlebrown-coyote-218911.hostingersite.com")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<PropietarioSalidaDto>> getPropietariosByUsername(@PathVariable String username) {
        List<PropietarioSalidaDto> propietarios =propietarioService.buscarPropietariosPorUsuario(username);
        return ResponseEntity.ok(propietarios);
    }
//
//    @PostMapping("/{id}/imagenes")
//    public ResponseEntity<?> subirImagenesAPropietario(@PathVariable Long id,
//                                                     @RequestParam("files") MultipartFile[] archivos) {
//        try {
//            List<String> urls = imagenService.subirImagenesYAsociarAPropietario(id, archivos);
//            return ResponseEntity.ok(urls);
//        } catch (Exception e) {
//            // 🔥 Imprimí el error para debug
//            e.printStackTrace();
//
//            // 🧠 Podés loguearlo con SLF4J si querés:
//            // log.error("Error al subir imágenes", e);
//
//            // 💬 Devolvés una respuesta clara al frontend
//            return ResponseEntity
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error al subir las imágenes: " + e.getMessage());
//        }
//    }
}
