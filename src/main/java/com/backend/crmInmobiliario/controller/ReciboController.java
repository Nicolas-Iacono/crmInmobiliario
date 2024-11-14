//package com.backend.crmInmobiliario.controller;
//
//import com.backend.crmInmobiliario.DTO.entrada.ReciboEntradaDto;
//import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
//import com.backend.crmInmobiliario.DTO.salida.ReciboSalidaDto;
//import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
//import com.backend.crmInmobiliario.service.impl.ReciboService;
//import com.backend.crmInmobiliario.utils.ApiResponse;
//import jakarta.validation.Valid;
//import lombok.AllArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@AllArgsConstructor
//@RequestMapping("/api/recibo")
//@CrossOrigin(origins = "http://localhost:3000")
//public class ReciboController {
//
//    private final ReciboService reciboService;
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
//
//    @CrossOrigin(origins = "http://localhost:3000")
//    @GetMapping("/byContract/{id}")
//    public ResponseEntity<ApiResponse<List<ReciboSalidaDto>>> allRecibos(@PathVariable Long id) throws ResourceNotFoundException {
//        List<ReciboSalidaDto> recibosSalidaDtos = reciboService.buscarRecibosPorNumDeContrato(id);
//        ApiResponse<List<ReciboSalidaDto>> response =
//                new ApiResponse<>("Lista de Recibos: ", recibosSalidaDtos);
//        return  ResponseEntity.status(HttpStatus.OK).body(response);
//    }
//
//    @CrossOrigin(origins = "http://localhost:3000")
//    @GetMapping("/all")
//    public ResponseEntity<ApiResponse<List<ReciboSalidaDto>>> allRecibos(){
//        List<ReciboSalidaDto> reciboSalidaDtos = reciboService.listarRecibos();
//        ApiResponse<List<ReciboSalidaDto>> response =
//                new ApiResponse<>("Lista de recibos: ", reciboSalidaDtos);
//        return  ResponseEntity.status(HttpStatus.OK).body(response);
//    }
//}
