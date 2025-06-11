package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.*;
import com.backend.crmInmobiliario.DTO.salida.*;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.impl.AguaService;
import com.backend.crmInmobiliario.service.impl.GasService;
import com.backend.crmInmobiliario.service.impl.LuzService;
import com.backend.crmInmobiliario.service.impl.MunicipalService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "https://darkgreen-ferret-296866.hostingersite.com")
@RestController
@AllArgsConstructor
@RequestMapping("api/impuesto")
public class ImpuestoController {
    private final static Logger LOGGER = LoggerFactory.getLogger(ImpuestoController.class);

    private final AguaService aguaService;
    private final GasService gasService;
    private final LuzService luzService;
    private final MunicipalService municipalService;



    @GetMapping("/agua/all")
    public ResponseEntity<ApiResponse<List<ImpuestoAguaSalidaDto>>> allContratosAgua(){
        List<ImpuestoAguaSalidaDto> impuestosAguasSalidaDtos = aguaService.listarImpuestoAgua();
        ApiResponse<List<ImpuestoAguaSalidaDto>> response =
                new ApiResponse<>("Lista de impuestos de agua: ", impuestosAguasSalidaDtos);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/gas/all")
    public ResponseEntity<ApiResponse<List<ImpuestoGasSalidaDto>>> allContratosGas(){
        List<ImpuestoGasSalidaDto> impuestosGasSalidaDtos = gasService.listarImpuestoGas();
        ApiResponse<List<ImpuestoGasSalidaDto>> response =
                new ApiResponse<>("Lista de impuestos de gas: ", impuestosGasSalidaDtos);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/luz/all")
    public ResponseEntity<ApiResponse<List<ImpuestoLuzSalidaDto>>> allContratosLuz(){
        List<ImpuestoLuzSalidaDto> impuestosLuzSalidaDtos = luzService.listarImpuestoLuz();
        ApiResponse<List<ImpuestoLuzSalidaDto>> response =
                new ApiResponse<>("Lista de impuestos de luz: ", impuestosLuzSalidaDtos);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/municipal/all")
    public ResponseEntity<ApiResponse<List<ImpuestoMunicipalSalidaDto>>> allContratosMunicipal(){
        List<ImpuestoMunicipalSalidaDto> impuestosMunicipalSalidaDtos = municipalService.listarImpuestoMunicipal();
        ApiResponse<List<ImpuestoMunicipalSalidaDto>> response =
                new ApiResponse<>("Lista de impuestos de municipal: ", impuestosMunicipalSalidaDtos);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/agua/create")
    public ResponseEntity<ApiResponse<ImpuestoAguaSalidaDto>> crearServicioDeAgua(@Valid @RequestBody ImpuestoAguaEntradaDto impuestoAguaEntradaDto) {
        LOGGER.info("Recibiendo solicitud para crear nuevo servicio de agua: {}", impuestoAguaEntradaDto);
        try {
            LOGGER.info("Servicio creado exitosamente");
            ImpuestoAguaSalidaDto impuestoAguaSalidaDto = aguaService.crearImpuestoAgua(impuestoAguaEntradaDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Servicio creado correctamente.", impuestoAguaSalidaDto));
        } catch (ResourceNotFoundException e) {
            LOGGER.error("Error al crear servicio: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("no se pudo crear el servicio", null));
        }
    }

    @PostMapping("/gas/create")
    public ResponseEntity<ApiResponse<ImpuestoGasSalidaDto>> crearServicioDeGas(@Valid @RequestBody ImpuestoGasEntradaDto impuestogasEntradaDto) {
        LOGGER.info("Recibiendo solicitud para crear nuevo servicio de gas: {}", impuestogasEntradaDto);
        try {
            LOGGER.info("Servicio creado exitosamente");
            ImpuestoGasSalidaDto impuestoGasSalidaDto = gasService.crearImpuestoGas(impuestogasEntradaDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Servicio creado correctamente.", impuestoGasSalidaDto));
        } catch (ResourceNotFoundException e) {
            LOGGER.error("Error al crear servicio: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("no se pudo crear el servicio", null));
        }
    }

    @PostMapping("/luz/create")
    public ResponseEntity<ApiResponse<ImpuestoLuzSalidaDto>> crearServicioDeLuz(@Valid @RequestBody ImpuestoLuzEntradaDto impuestoLuzEntradaDto) {
        LOGGER.info("Recibiendo solicitud para crear nuevo servicio de luz: {}", impuestoLuzEntradaDto);
        try {
            LOGGER.info("Servicio creado exitosamente");
            ImpuestoLuzSalidaDto impuestoLuzSalidaDto = luzService.crearImpuestoLuz(impuestoLuzEntradaDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Servicio creado correctamente.", impuestoLuzSalidaDto));
        } catch (ResourceNotFoundException e) {
            LOGGER.error("Error al crear servicio: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("no se pudo crear el servicio", null));
        }
    }

    @PostMapping("/municipal/create")
    public ResponseEntity<ApiResponse<ImpuestoMunicipalSalidaDto>> crearServicioMunicipal(@Valid @RequestBody ImpuestoMunicipalEntradaDto impuestoMunicipalEntradaDto) {
        LOGGER.info("Recibiendo solicitud para crear nuevo servicio de luz: {}", impuestoMunicipalEntradaDto);
        try {
            LOGGER.info("Servicio creado exitosamente");
            ImpuestoMunicipalSalidaDto impuestoMunicipalSalidaDto = municipalService.crearImpuestoMunicipal(impuestoMunicipalEntradaDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Servicio creado correctamente.", impuestoMunicipalSalidaDto));
        } catch (ResourceNotFoundException e) {
            LOGGER.error("Error al crear servicio: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("no se pudo crear el servicio", null));
        }
    }

}
