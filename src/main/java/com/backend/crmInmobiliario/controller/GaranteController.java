package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.garante.GaranteEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.impl.ContratoService;
import com.backend.crmInmobiliario.service.impl.GaranteService;
import com.backend.crmInmobiliario.service.impl.ImagenService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "https://darkgreen-ferret-296866.hostingersite.com")
@RequestMapping("api/garante")
public class GaranteController {
    private final GaranteService garanteService;
    private final ContratoService contratoService;
    private final ImagenService imagenService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<GaranteSalidaDto>>> allGarantes(){
        List<GaranteSalidaDto> garanteSalidaDtos = garanteService.listarGarantes();
        ApiResponse<List<GaranteSalidaDto>> response =
                new ApiResponse<>("Lista de garantes: ", garanteSalidaDtos);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<GaranteSalidaDto>> crearGarante(@Valid @RequestBody GaranteEntradaDto garanteEntradaDto) {
        try {
            GaranteSalidaDto garanteSalidaDto = garanteService.crearGarante(garanteEntradaDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Garante creado correctamente.", garanteSalidaDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("error not found", null));
        }
    }

    @PostMapping("/agregarGarante/{idContrato}/{idGarante}")
    public ResponseEntity<ApiResponse<String>> asignarGarante(@PathVariable Long idContrato, @PathVariable Long idGarante) {
        try {
            garanteService.asignarGarante(idGarante, idContrato);
            return ResponseEntity.ok(new ApiResponse<>("Garante asignado a contrato", "Exito"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Error: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno del servidor", null));
        }
    }
    @GetMapping("/buscar/{id}")
    public ResponseEntity<ApiResponse<GaranteSalidaDto>> buscarGarantePorId(@PathVariable Long id){
        try{
            GaranteSalidaDto garanteSalidaDto = garanteService.listarGarantePorId(id);
            return  ResponseEntity.ok(new ApiResponse<>("Garante encontrado, ", garanteSalidaDto));
        }catch (ResourceNotFoundException  e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("No se encontro el garante buscado, ", null));
        }
    }

    @CrossOrigin(origins = "https://darkgreen-ferret-296866.hostingersite.com")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<?>> eliminarGarante(@PathVariable Long id) {
        try {
          garanteService.eliminarGarante(id);
            return ResponseEntity.ok(new ApiResponse<>("Garabte con ID: " + id + " eliminado correctamente.", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Garante no encontrado con el ID: " + id, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al eliminar el Garante", null));
        }
    }

    @GetMapping("/{username}")
    @CrossOrigin(origins = "https://darkgreen-ferret-296866.hostingersite.com")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<GaranteSalidaDto>> getGarantesByUsername(@PathVariable String username) {
        List<GaranteSalidaDto> garantes = garanteService.buscarGarantePorUsuario(username);
        return ResponseEntity.ok(garantes);
    }

//    @PostMapping("/{id}/imagenes")
//    public ResponseEntity<?> subirImagenesAGarante(@PathVariable Long id,
//                                                   @RequestParam("files") MultipartFile[] archivos) {
//        try {
//            List<String> urls = imagenService.subirImagenesYAsociarAGarante(id, archivos);
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
