package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.ReciboEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.ReciboModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.ReciboSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.impl.ReciboService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/recibo")
@CrossOrigin(origins = "https://tuinmo.net")
public class ReciboController {

    private final ReciboService reciboService;
//
//    @PostMapping("/create")
//    public ResponseEntity<ApiResponse<ReciboSalidaDto>> crearRecibo(@Valid @RequestBody ReciboEntradaDto reciboEntradaDto){
//        try{
//            ReciboSalidaDto reciboSalidaDto = reciboService.crearRecibo(reciboEntradaDto);
//            return ResponseEntity.status(HttpStatus.CREATED)
//                    .body(new ApiResponse<>("Recibo creado correctamente.", reciboSalidaDto));
//        }catch (ResourceNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(new ApiResponse<>("El contrato no se encontro", null));
//        }
//    }
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ReciboSalidaDto>> crearRecibo(@Valid @RequestBody ReciboEntradaDto reciboEntradaDto) throws ResourceNotFoundException {
        ReciboSalidaDto reciboSalidaDto = reciboService.crearRecibo(reciboEntradaDto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Recibo creado correctamente", reciboSalidaDto));
    }
    @GetMapping("/{id}") // Usa /{id} para indicar un parámetro en la URL
    public ResponseEntity<ApiResponse<ReciboSalidaDto>> buscarReciboPorId(@PathVariable Long id) throws ResourceNotFoundException {
        ReciboSalidaDto reciboSalidaDto = reciboService.buscarReciboPorId(id);
        ApiResponse<ReciboSalidaDto> response = new ApiResponse<>(true, "Recibo encontrado exitosamente", reciboSalidaDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ReciboSalidaDto>>> allRecibos(){
        List<ReciboSalidaDto> reciboSalidaDtos = reciboService.listarRecibos();
        ApiResponse<List<ReciboSalidaDto>> response =
                new ApiResponse<>("Lista de recibos: ", reciboSalidaDtos);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }


    /**
     * Endpoint para modificar el estado de un recibo.
     *
     * @param reciboModificacionDto contiene el id del recibo y el nuevo estado.
     * @return ReciboSalidaDto con el recibo actualizado.
     */
    @PutMapping("/estado")
    public ResponseEntity<ReciboSalidaDto> modificarEstado(@RequestBody ReciboModificacionDto reciboModificacionDto) {
        try {
            ReciboSalidaDto reciboActualizado = reciboService.modificarEstado(reciboModificacionDto);
            return ResponseEntity.ok(reciboActualizado);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
