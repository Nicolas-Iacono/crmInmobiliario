package com.backend.crmInmobiliario.controller;


import com.backend.crmInmobiliario.DTO.entrada.propiedades.PropiedadEntradaDto;
import com.backend.crmInmobiliario.DTO.salida.PropiedadSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.PropiedadSoloSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.impl.PropiedadService;
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
@RequestMapping("/api/propiedad")
public class PropiedadController {
    private final PropiedadService propiedadService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PropiedadSalidaDto>> crearPropiedad(@Valid @RequestBody PropiedadEntradaDto propiedadEntradaDto) {
        try {
            PropiedadSalidaDto propiedadSalidaDto = propiedadService.crearPropiedad(propiedadEntradaDto, propiedadEntradaDto.getId_propietario());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Propiedad creada correctamente.", propiedadSalidaDto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("El propietario no se encuentra en la base de datos", null));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PropiedadSoloSalidaDto>>> allPropiedades(){
        List<PropiedadSoloSalidaDto> propiedadesSalidaDtos = propiedadService.listarPropiedades();
        ApiResponse<List<PropiedadSoloSalidaDto>> response =
                new ApiResponse<>("Lista de propietarios: ", propiedadesSalidaDtos);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

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
    @CrossOrigin(origins = "http://localhost:3000")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<PropiedadSalidaDto>> getPropiedadByUsername(@PathVariable String username) {
        List<PropiedadSalidaDto> propiedades =propiedadService.buscarPropiedadesPorUsuario(username);
        return ResponseEntity.ok(propiedades);
    }
}
