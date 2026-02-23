package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.aliados.OficioProveedorCreateDto;
import com.backend.crmInmobiliario.DTO.entrada.aliados.OficioServicioCreateDto;
import com.backend.crmInmobiliario.DTO.modificacion.aliados.OficioServicioUpdateDto;
import com.backend.crmInmobiliario.DTO.salida.aliados.OficioProveedorSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.aliados.OficioServicioSalidaDto;
import com.backend.crmInmobiliario.service.IOficioServicioService;
import com.backend.crmInmobiliario.service.impl.aliados.OficioServicioServiceImpl;
import com.backend.crmInmobiliario.utils.AuthUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/oficios")
@CrossOrigin(origins = "https://tuinmo.net")
@RequiredArgsConstructor
public class OficioServicioController {

    private final IOficioServicioService servicioService;
    private final AuthUtil authUtil;
    private final ObjectMapper objectMapper;
    private final OficioServicioServiceImpl oficioServicioService;

    @PostMapping("/proveedores/registro")
    @PreAuthorize("permitAll()")
    public ResponseEntity<OficioProveedorSalidaDto> registrarProveedor(
            @Valid @RequestBody OficioProveedorCreateDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(oficioServicioService.registrarProveedor(dto));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OficioServicioSalidaDto> crear(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "imagenes", required = false) MultipartFile[] imagenes
    ) throws IOException {
        Long userId = authUtil.extractUserId();
        OficioServicioCreateDto dto = objectMapper.readValue(dataJson, OficioServicioCreateDto.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(servicioService.crear(userId, dto, imagenes));
    }

    @GetMapping
    public ResponseEntity<List<OficioServicioSalidaDto>> listar() {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(servicioService.listarMisServicios(userId));
    }

    @GetMapping("/{servicioId}")
    public ResponseEntity<OficioServicioSalidaDto> obtener(@PathVariable Long servicioId) {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(servicioService.obtenerMiServicio(userId, servicioId));
    }

    @PutMapping(value = "/{servicioId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OficioServicioSalidaDto> editar(
            @PathVariable Long servicioId,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "imagenes", required = false) MultipartFile[] imagenes
    ) throws IOException {
        Long userId = authUtil.extractUserId();
        OficioServicioUpdateDto dto = objectMapper.readValue(dataJson, OficioServicioUpdateDto.class);
        return ResponseEntity.ok(servicioService.editar(userId, servicioId, dto, imagenes));
    }

    @DeleteMapping("/{servicioId}")
    public ResponseEntity<Void> eliminar(@PathVariable Long servicioId) {
        Long userId = authUtil.extractUserId();
        servicioService.eliminar(userId, servicioId);
        return ResponseEntity.noContent().build();
    }





}

