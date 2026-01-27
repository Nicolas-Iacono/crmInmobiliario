package com.backend.crmInmobiliario.DTO.entrada.contrato;

import java.time.LocalDate;

public record ContratoEventoRow(

        Long id,
        String nombreContrato,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        int actualizacion
) {
}
