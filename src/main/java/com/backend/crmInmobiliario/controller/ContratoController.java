package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.contrato.ContratoEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.ContratoModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoActualizacionDtoSalida;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaSinGaranteDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.LatestContratosSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.impl.ContratoService;
import com.backend.crmInmobiliario.service.impl.GaranteService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "https://darkgreen-ferret-296866.hostingersite.com")
@RestController
@AllArgsConstructor
@RequestMapping("api/contrato")
public class ContratoController {

    private final static Logger LOGGER = LoggerFactory.getLogger(ContratoController.class);
    private final GaranteService garanteService;
    private final ContratoService contratoService;

    @Transactional
    @GetMapping("/enum/{username}")
    public Integer enumerarContratos(@PathVariable String username) {
        Integer contratos = contratoService.enumerarContratos(username);
        return contratos;
    }
    @Transactional
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ContratoSalidaDto>>> allContratos() {
        List<ContratoSalidaDto> contratosSalidaDtos = contratoService.listarContratos();
        ApiResponse<List<ContratoSalidaDto>> response =
                new ApiResponse<>("Lista de contratos: ", contratosSalidaDtos);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @Transactional
    @PutMapping("/actualizacion")
    public ResponseEntity<ContratoSalidaDto> actualizarMontoAlquiler(
                                                                     @RequestBody ContratoModificacionDto dto) {
        try {
            ContratoSalidaDto actualizado = contratoService.actualizarMontoAlquiler(dto);
            return ResponseEntity.ok(actualizado);
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @Transactional
    @GetMapping("/buscar/{id}")
    public ResponseEntity<ApiResponse<ContratoSalidaDto>> buscarContratoPorId(@PathVariable Long id) {
        ContratoSalidaDto contratoSalidaDto = contratoService.buscarContratoPorId(id);
        return ResponseEntity.ok(new ApiResponse<>("contrato encontrado, ", contratoSalidaDto));
    }
    @Transactional
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ContratoSalidaDto>> crearContrato(@Valid @RequestBody ContratoEntradaDto contratoEntradaDto) {
        LOGGER.info("Recibiendo solicitud para crear contrato: {}", contratoEntradaDto);
        try {
            LOGGER.info("Contrato creado exitosamente");
            ContratoSalidaDto contratoSalidaDto = contratoService.crearContrato(contratoEntradaDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Contrato creado correctamente.", contratoSalidaDto));
        } catch (ResourceNotFoundException e) {
            LOGGER.error("Error al crear contrato: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("no se pudo crear el contrato", null));
        }
    }
    @Transactional
    @GetMapping("/verificar-contrato/{id}")
    public ResponseEntity<?> VerificarFinalizacionContrato(@PathVariable Long id) {
        try {
            Long mensaje = contratoService.verificarFinalizacionContrato(id);

            return ResponseEntity.ok(mensaje);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @Transactional
    @GetMapping("/verificar-actualizacion/{id}")
    public ResponseEntity<ApiResponse<ContratoActualizacionDtoSalida>> VerificarActualizacionContrato(@PathVariable Long id) throws ResourceNotFoundException {
        ContratoActualizacionDtoSalida actualizacion = contratoService.verificarActualizacionContrato(id);
        ApiResponse<ContratoActualizacionDtoSalida> response = new ApiResponse<>("actualizacion", actualizacion);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @Transactional
    @PostMapping("/finalizar/{id}")
    public ResponseEntity<String> FinalizarContrato(@PathVariable Long id) {
        try {
            contratoService.finalizarContrato(id);
            return ResponseEntity.ok("Contrato finalizado correctamente");

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
    @Transactional
    @GetMapping("/{username}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ContratoSalidaDto>> getContratosByUsername(@PathVariable String username) {
        List<ContratoSalidaDto> contratos = contratoService.buscarContratoPorUsuario(username);
        return ResponseEntity.ok(contratos);
    }
    @Transactional
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<?>> eliminarContrato(@PathVariable Long id) {
        try {
            garanteService.deleteByContratoId(id); // Implementa este método en tu servicio

            // Luego elimina el contrato
            contratoService.eliminarContrato(id);
            return ResponseEntity.ok(new ApiResponse<>("Contrato con ID: " + id + " eliminado correctamente.", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Contrato no encontrado con el ID: " + id, null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>("No se puede eliminar un contrato activo.", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al eliminar el contrato", null));
        }
    }
    @Transactional
    @PatchMapping("/{id}/updateContract")
    public ResponseEntity<ApiResponse<ContratoSalidaDto>> actualizarPdfContrato(@PathVariable Long id, @RequestBody ContratoModificacionDto updateDto) throws ResourceNotFoundException {
        ContratoSalidaDto contratoActualizado = contratoService.guardarContratoPdf(id, updateDto);
        ApiResponse<ContratoSalidaDto> response = new ApiResponse<>(true, "Contrato actualizado con éxito", contratoActualizado);
        return ResponseEntity.ok(response);
    }
    @Transactional
    @GetMapping("/latest")
    @PreAuthorize("permitAll()")
    public List<LatestContratosSalidaDto> getLatestContratos() {
        return contratoService.getLatestContratos();
    }
}