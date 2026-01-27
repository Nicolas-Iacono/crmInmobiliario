package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.contrato.*;
import com.backend.crmInmobiliario.DTO.modificacion.ContratoModificacionDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.*;
import com.backend.crmInmobiliario.entity.Contrato;
import com.backend.crmInmobiliario.entity.EstadoContrato;
import com.backend.crmInmobiliario.entity.Usuario;
import com.backend.crmInmobiliario.exception.ContractLimitExceededException;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.IUsuarioService;
import com.backend.crmInmobiliario.service.impl.ContratoService;
import com.backend.crmInmobiliario.service.impl.GaranteService;
import com.backend.crmInmobiliario.service.impl.ReciboService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import com.backend.crmInmobiliario.utils.AuthUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CrossOrigin(origins = "https://tuinmo.net")
@RestController
@RequestMapping("api/contrato")
public class ContratoController {

    private final static Logger LOGGER = LoggerFactory.getLogger(ContratoController.class);
    private final GaranteService garanteService;
    private final ContratoService contratoService;
    private final ReciboService reciboService;
    private final AuthUtil authUtil;
    private final IUsuarioService usuarioService;
    private final ModelMapper modelMapper;

    public ContratoController(GaranteService garanteService, ContratoService contratoService, ReciboService reciboService, AuthUtil authUtil, IUsuarioService usuarioService, ModelMapper modelMapper) {
        this.garanteService = garanteService;
        this.contratoService = contratoService;
        this.reciboService = reciboService;
        this.authUtil = authUtil;
        this.usuarioService = usuarioService;
        this.modelMapper = modelMapper;
    }


    @PutMapping("/mod/{id}")
    public ResponseEntity<Void> editarContrato(
            @PathVariable Long id,
            @RequestBody ContratoModificacionDto dto
    ) throws ResourceNotFoundException {

        dto.setIdContrato(id); // por si lo querés usar después
       contratoService.editarContrato(id, dto);
       return ResponseEntity.ok().build();
    }


    @GetMapping("/pdf/{id}")
    public ResponseEntity<ApiResponse<ContratoPdfDto>> buscarContratoPdf(@PathVariable Long id) {
        ContratoPdfDto dto = contratoService.buscarContratoPdf(id);
        return ResponseEntity.ok(new ApiResponse<>("Contrato PDF listo", dto));
    }


    @GetMapping("/generar-embeddings")
    public ResponseEntity<?> generarEmbeddings() {
        try {
            Long userId = authUtil.extractUserId();
            contratoService.generarEmbeddingsParaUsuario(userId);
            return ResponseEntity.ok("✅ Embeddings generados correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }



    @PutMapping("/comisiones")
    public ResponseEntity<ContratoComisionDtoSalida> actualizarComisiones(
            @RequestBody ContratoComisionUpdateDto dto) {
        return ResponseEntity.ok(contratoService.actualizarComisiones(dto));
    }

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
            ContratoSalidaDto contratoSalidaDto = contratoService.crearContrato(contratoEntradaDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Contrato creado correctamente.", contratoSalidaDto));

        } catch (ContractLimitExceededException e) {
            LOGGER.warn("Límite de contratos alcanzado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(e.getMessage(), null));

        } catch (ResourceNotFoundException e) {
            LOGGER.error("Error al crear contrato: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("No se pudo crear el contrato", null));

        } catch (Exception e) {
            LOGGER.error("Error inesperado al crear contrato: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al crear contrato", null));
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
    @DeleteMapping("/delete-forzado/{id}")
    public ResponseEntity<ApiResponse<?>> eliminarContratoForzado(@PathVariable Long id) {
        try {
            // Paso 1: Finalizar el contrato (lo marca como inactivo y libera la propiedad)
            contratoService.finalizarContrato(id);

            // Paso 2: Eliminar el contrato (ya inactivo)
            contratoService.eliminarContrato(id);

            return ResponseEntity.ok(new ApiResponse<>("✅ Contrato activo eliminado de forma forzada.", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Contrato no encontrado con el ID: " + id, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("❌ Error al eliminar el contrato de forma forzada: " + e.getMessage(), null));
        }
    }

    @Transactional
    @PutMapping("/{id}/updateContract")
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

    @CrossOrigin(origins = "https://tuinmo.net")
    @GetMapping("/buscar-por-nombre")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ContratoSalidaDto> obtenerContratoPorNombre(@RequestParam String nombre) {
        try {
            ContratoSalidaDto dto = contratoService.buscarContratoPorNombre(nombre);
            return ResponseEntity.ok(dto);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ContratoSalidaDto>> listarMisContratos() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getDetails() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        Long userId = (Long) details.get("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        List<ContratoSalidaDto> contratos = contratoService.listarContratosPorUsuarioId(userId);

        return ResponseEntity.ok(contratos);
    }


    @GetMapping("/me/cards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ContratoCardDto>> listarMisCards() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getDetails() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        Long userId = (Long) details.get("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        List<ContratoCardDto> contratos = contratoService.listarCardscontratos(userId);

        return ResponseEntity.ok(contratos);
    }


    @GetMapping("/eventos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ContractEventDto>> eventosContratos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getDetails() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        Long userId = (Long) details.get("userId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        List<ContractEventDto> eventos =
                contratoService.getEventosContratos(userId, from, to);

        return ResponseEntity.ok(eventos);
    }

    @PutMapping("/{id}/estados")
    public ResponseEntity<ContratoEstadosDto> actualizarEstados(
            @PathVariable Long id,
            @RequestBody Set<EstadoContrato> estados
    ) {
        ContratoEstadosDto dto = contratoService.actualizarEstados(id, estados);
        return ResponseEntity.ok(dto);
    }


//    @PostMapping("/renovar/{id}")
//    @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<ContratoSalidaDto> renovarContrato(
//            @PathVariable Long id,
//            @RequestBody RenovarContratoRequest req
//    ) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || authentication.getDetails() == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//
//        @SuppressWarnings("unchecked")
//        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
//        Long userId = (Long) details.get("userId");
//
//        if (userId == null) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//        }
//
//        ContratoSalidaDto dto = contratoService.renovarContrato(id, req, userId);
//        return ResponseEntity.ok(dto);
//    }


    @Transactional
    @GetMapping("/alertas-vencimiento")
    public ResponseEntity<ApiResponse<List<ContratoVencimientoAlertaDto>>> obtenerAlertasVencimiento(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "30") int dias) {
        try {
            List<ContratoVencimientoAlertaDto> alertas = contratoService.obtenerAlertasVencimiento(userId, dias);
            ApiResponse<List<ContratoVencimientoAlertaDto>> response =
                    new ApiResponse<>("alertas_vencimiento", alertas);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al obtener alertas", null));
        }
    }

    @Transactional
    @PutMapping("/alertas-vencimiento/estado")
    public ResponseEntity<ApiResponse<ContratoVencimientoAlertaDto>> actualizarEstadoAlerta(
            @RequestBody ContratoAlertaEstadoDto dto) {
        try {
            ContratoVencimientoAlertaDto alerta = contratoService.actualizarEstadoAlerta(dto);
            return ResponseEntity.ok(new ApiResponse<>("alerta_actualizada", alerta));
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al actualizar la alerta", null));
        }
    }


    @Transactional
    @PostMapping("/renovar")
    public ResponseEntity<ApiResponse<ContratoSalidaDto>> renovarContrato(
            @RequestBody ContratoRenovacionDtoEntrada dto) {
        try {
            ContratoSalidaDto renovado = contratoService.renovarContrato(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Contrato renovado correctamente.", renovado));
        } catch (ResourceNotFoundException | IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Error interno al renovar el contrato", null));
        }
    }
}