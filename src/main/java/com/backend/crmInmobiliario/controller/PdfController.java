//package com.backend.crmInmobiliario.controller;
//
//import com.backend.crmInmobiliario.DTO.entrada.ContratoPdfEntradaDto;
//import com.backend.crmInmobiliario.DTO.salida.ContratoPdfSalidaDto;
//import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
//import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
//import com.backend.crmInmobiliario.service.impl.PdfService;
//import com.backend.crmInmobiliario.utils.ApiResponse;
//import jakarta.validation.Valid;
//import lombok.AllArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@CrossOrigin(origins = "http://localhost:3000")
//@RestController
//@AllArgsConstructor
//@RequestMapping("api/pdf")
//public class PdfController {
//    private final static Logger LOGGER = LoggerFactory.getLogger(PdfController.class);
//    private final PdfService pdfService;
////
//
//    @PostMapping("/create")
//    public ResponseEntity<ApiResponse<ContratoPdfSalidaDto>> guardarPdf(@Valid @RequestBody ContratoPdfEntradaDto contratoPdfEntradaDto){
//        LOGGER.info("Recibiendo solicitud para crear pdf: {}", contratoPdfEntradaDto);
//
//        try{
//           LOGGER.info("PDF creado correctamente");
//           ContratoPdfSalidaDto pdfSalidaDto = pdfService.guardarPdf(contratoPdfEntradaDto);
//            return ResponseEntity.status(HttpStatus.CREATED)
//                    .body(new ApiResponse<>("Contrato creado correctamente.", pdfSalidaDto));
//        } catch (ResourceNotFoundException e) {
//            LOGGER.error("Error al crear pdf: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(new ApiResponse<>("no se pudo crear el contrato", null));
//        }
//    }
//
//    @GetMapping("/all")
//    public ResponseEntity<ApiResponse<List<ContratoPdfSalidaDto>>> allContratos(){
//        List<ContratoPdfSalidaDto> pdfSalidaDtos = pdfService.listarPdf();
//        ApiResponse<List<ContratoPdfSalidaDto>> response =
//                new ApiResponse<>("Lista de PDF: ", pdfSalidaDtos);
//        return  ResponseEntity.status(HttpStatus.OK).body(response);
//    }
//}
