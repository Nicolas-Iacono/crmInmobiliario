package com.backend.crmInmobiliario.DTO.salida.recibo;

import com.backend.crmInmobiliario.entity.TipoAlertaRecibo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReciboAlertaDto(
        Long id,
        Long reciboId,
        Long contratoId,
        Long usuarioId,
        TipoAlertaRecibo tipo,
        boolean visto,
        boolean noMostrar,
        LocalDate ultimaNotificacion,
        LocalDateTime fechaCreacion
) {
}
