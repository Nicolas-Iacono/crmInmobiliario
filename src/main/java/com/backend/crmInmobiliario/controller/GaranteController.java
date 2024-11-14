package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.garante.GaranteEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.garante.GaranteSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.impl.ContratoService;
import com.backend.crmInmobiliario.service.impl.GaranteService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("api/garante")
public class GaranteController {
    private final GaranteService garanteService;
    private final ContratoService contratoService;


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


    @CrossOrigin(origins = "http://localhost:3000")
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
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<GaranteSalidaDto>> getGarantesByUsername(@PathVariable String username) {
        List<GaranteSalidaDto> garantes = garanteService.buscarGarantePorUsuario(username);
        return ResponseEntity.ok(garantes);
    }
}
