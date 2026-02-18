package com.backend.crmInmobiliario.controller;

import com.backend.crmInmobiliario.DTO.entrada.ReciboEntradaDto;
import com.backend.crmInmobiliario.DTO.modificacion.ReciboModificacionDto;
import com.backend.crmInmobiliario.DTO.mpDtos.transferencias.TransferenciaNotificarDto;
import com.backend.crmInmobiliario.DTO.mpDtos.transferencias.entrada.NotificarTransferenciaDto;
import com.backend.crmInmobiliario.DTO.mpDtos.transferencias.entrada.RechazarTransferenciaDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.ContratoSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.ReciboSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.contrato.LatestContratosSalidaDto;
import com.backend.crmInmobiliario.DTO.salida.recibo.LatestRecibosSalidaDto;
import com.backend.crmInmobiliario.exception.ResourceNotFoundException;
import com.backend.crmInmobiliario.service.impl.ReciboService;
import com.backend.crmInmobiliario.service.impl.mercadoPago.MercadoPagoReciboService;
import com.backend.crmInmobiliario.utils.ApiResponse;
import com.backend.crmInmobiliario.utils.ApiResponseRecibo;
import com.backend.crmInmobiliario.utils.AuthUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@RestController
@AllArgsConstructor
@RequestMapping("/api/recibo")
@CrossOrigin(origins = "https://tuinmo.net")
public class ReciboController {
    private final AuthUtil authUtil;
    private final MercadoPagoReciboService mercadoPagoReciboService;

    private final ReciboService reciboService;
    private final Logger LOGGER = LoggerFactory.getLogger(ReciboService.class);
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
@PostMapping(value = "/create", consumes = "multipart/form-data")
public ResponseEntity<ApiResponse<ReciboSalidaDto>> crearRecibo(@Valid @ModelAttribute ReciboEntradaDto reciboEntradaDto)
        throws ResourceNotFoundException, IOException {

    LOGGER.warn("Fecha emisión recibida: {}", reciboEntradaDto.getFechaEmision());
    LOGGER.warn("Fecha vencimiento recibida: {}", reciboEntradaDto.getFechaVencimiento());

    ReciboSalidaDto reciboSalidaDto = reciboService.crearRecibo(reciboEntradaDto);


    return ResponseEntity.ok(new ApiResponse<>(true, "Recibo creado correctamente", reciboSalidaDto));
}
    @GetMapping("/{id}") // Usa /{id} para indicar un parámetro en la URL
    public ResponseEntity<ApiResponse<ReciboSalidaDto>> buscarReciboPorId(@PathVariable Long id) throws ResourceNotFoundException {
        ReciboSalidaDto reciboSalidaDto = reciboService.buscarReciboPorId(id);

        ApiResponse<ReciboSalidaDto> response =
                new ApiResponse<>( "Recibo encontrado exitosamente", reciboSalidaDto);
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

    @GetMapping("/estado")
    public ResponseEntity<ApiResponseRecibo<List<ReciboSalidaDto>>> listarPorEstado(
            @RequestParam Long userId,
            @RequestParam(required = false) Boolean estado,
            @RequestParam(required = false) Long contratoId,
            @RequestParam(required = false) String q
    ) {
        var data = reciboService.recibosDelUsuario(userId, estado, contratoId, q);
        return ResponseEntity.ok(new ApiResponseRecibo<>(true, "OK", data));
    }
    @GetMapping("/por-contrato/{contratoId}")
    public ResponseEntity<?> getRecibosPorContrato(@PathVariable Long contratoId) {
        try {
            List<ReciboSalidaDto> recibos = reciboService.listarRecibosPorContrato(contratoId);
            return ResponseEntity.ok(recibos);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "No se encontraron recibos o el contrato no existe",
                    "detalle", e.getMessage()
            ));
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReciboSalidaDto>> listarMisRecibos() {
        Long userId = authUtil.extractUserId();
        return ResponseEntity.ok(reciboService.listarRecibosPorUsuarioId(userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> eliminarRecibo(@PathVariable Long id) throws ResourceNotFoundException {

        reciboService.eliminarRecibo(id);

        return ResponseEntity.ok(
                new ApiResponse<>("Recibo eliminado correctamente", null)
        );
    }
    @PostMapping("/{reciboId}/pagar/mp")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> iniciarPagoMercadoPago(@PathVariable Long reciboId) {
        try {
            Long idUserInquilino = authUtil.extractUserId(); // si no existe, te dejo alternativa abajo
            var resp = mercadoPagoReciboService.crearLinkPagoRecibo(reciboId, idUserInquilino);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "No se pudo iniciar el pago",
                    "detalle", e.getMessage()
            ));
        }
    }

//    @PostMapping("/{reciboId}/pago-transferencia/notificar")
//    @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<?> notificarTransferencia(
//            @PathVariable Long reciboId,
//            @RequestBody TransferenciaNotificarDto dto
//    ) {
//        Long userIdInquilino = authUtil.extractUserId();
//        reciboService.notificarTransferencia(reciboId, userIdInquilino, dto);
//        return ResponseEntity.ok(Map.of("ok", true));
//    }

    @PostMapping("/{reciboId}/pagar/transferencia/notificar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> notificarTransferencia(
            @PathVariable Long reciboId,
            @Valid @RequestBody NotificarTransferenciaDto dto
    ) {
        try {
            Long userId = authUtil.extractUserId();
            reciboService.notificarTransferencia(reciboId, userId, dto);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "No se pudo notificar la transferencia",
                    "detalle", e.getMessage()
            ));
        }
    }
    @PatchMapping("/{reciboId}/transferencia/aprobar")
    public ResponseEntity<ApiResponse<ReciboSalidaDto>> aprobarTransferencia(
            @PathVariable Long reciboId

    ) {
        Long adminUserId = authUtil.extractUserId();
        ReciboSalidaDto out = reciboService.aprobarTransferencia(reciboId, adminUserId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Transferencia aprobada y recibo marcado como pagado", out));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')") // o el rol que use tu inmobiliaria
    @PatchMapping("/{reciboId}/transferencia/rechazar")
    public ResponseEntity<ApiResponse<ReciboSalidaDto>> rechazarTransferencia(@PathVariable Long reciboId) {
        Long adminUserId = authUtil.extractUserId();
        ReciboSalidaDto out = reciboService.rechazarTransferencia(reciboId, adminUserId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Transferencia rechazada", out));
    }

    @Transactional
    @GetMapping("/latest")
    @PreAuthorize("permitAll()")
    public List<LatestRecibosSalidaDto> getLatestRecibos() {
        return reciboService.getLatestRecibos();
    }



    @GetMapping("/ultimos-by-contrato")
    public ResponseEntity<List<LatestRecibosSalidaDto>> getLatestRecibosPorContrato() {
        return ResponseEntity.ok(reciboService.getLatestRecibosPorContrato());
    }
}
