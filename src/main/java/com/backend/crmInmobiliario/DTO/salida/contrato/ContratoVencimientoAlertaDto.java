package com.backend.crmInmobiliario.DTO.salida.contrato;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ContratoVencimientoAlertaDto {
    private Long id;
    private Long contratoId;
    private Long userId;
    private String nombreContrato;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private long diasRestantes;
    private boolean vencido;
    private String estado;
    private boolean renovable;
    private boolean finalizable;

    public ContratoVencimientoAlertaDto(
            Long id,
            Long contratoId,
            Long userId,
            String nombreContrato,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            long diasRestantes,
            boolean vencido,
            String estado,
            boolean renovable,
            boolean finalizable
    ) {
        this.id = id;
        this.contratoId = contratoId;
        this.userId = userId;
        this.nombreContrato = nombreContrato;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.diasRestantes = diasRestantes;
        this.vencido = vencido;
        this.estado = estado;
        this.renovable = renovable;
        this.finalizable = finalizable;
    }


}
