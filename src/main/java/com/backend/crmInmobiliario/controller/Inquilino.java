package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.InquilinoEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.inquilino.InquilinoSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.impl.ImagenService;
import com.backend.crmInmobiliario.service.impl.InquilinoService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/inquilino")
public class Inquilino {

    private final InquilinoService inquilinoService;
    private final ImagenService imagenService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<InquilinoSalidaDto>> crearInquilino(@Valid @RequestBody InquilinoEntradaDto inquilinoEntradaDto) {
        try {
            InquilinoSalidaDto inquilinoSalidaDto = inquilinoService.crearInquilino(inquilinoEntradaDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Inquilino creado correctamente.", inquilinoSalidaDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("El idCategory o idTheme no se encuentra en la DB", null));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<InquilinoSalidaDto>>> allInquilinos(){
        List<InquilinoSalidaDto> inquilinosSalidaDtos = inquilinoService.listarInquilinos();
        ApiResponse<List<InquilinoSalidaDto>> response =
                new ApiResponse<>("Lista de inquilinos: ", inquilinosSalidaDtos);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<?>> eliminarInquilino(@PathVariable Long id){
        try{
            inquilinoService.eliminarInquilino(id);
            return  ResponseEntity.ok(new ApiResponse<>("Inquilino con ID: " + id + " eliminado.", null ));
        }catch (ResourceNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Inquilino no encontrado con el ID: " + id, null));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al eliminar el inquilino", null));
        }
    }

    @GetMapping("/buscar/{id}")
    public ResponseEntity<ApiResponse<InquilinoSalidaDto>> buscarInquilinoPorId(@PathVariable Long id){
        try{
            InquilinoSalidaDto inquilinoBuscado = inquilinoService.buscarInquilinoPorId(id);
           return  ResponseEntity.ok(new ApiResponse<>("Inquilino encontrado, ", inquilinoBuscado));
        }catch (ResourceNotFoundException  e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("No se encontro el inquilino buscado, ", null));
        }
    }

    @GetMapping("/{username}")
    @CrossOrigin(origins = "https://saddlebrown-coyote-218911.hostingersite.com")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<InquilinoSalidaDto>> getInquilinoByUsername(@PathVariable String username) {
        List<InquilinoSalidaDto> inquilinos = inquilinoService.buscarInquilinoPorUsuario(username);
        return ResponseEntity.ok(inquilinos);
    }
//
//    @PostMapping("/{id}/imagenes")
//    public ResponseEntity<?> subirImagenesAInquilino(@PathVariable Long id,
//                                                     @RequestParam("files") MultipartFile[] archivos) {
//        try {
//            List<String> urls = imagenService.subirImagenesYAsociarAInquilino(id, archivos);
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
