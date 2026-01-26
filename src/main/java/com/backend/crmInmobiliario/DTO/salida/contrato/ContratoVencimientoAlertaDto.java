package com.backend.crmInmobiliario.DTO.salida.contrato;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContratoVencimientoAlertaDto {
    private Long idContrato;
    private String nombreContrato;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private long diasRestantes;
    private boolean vencido;
    private String estado;
    private boolean renovable;
    private boolean finalizable;
}
