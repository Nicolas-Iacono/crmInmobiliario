package com.backend.crmInmobiliario.DTO.salida.contrato;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    public ContratoVencimientoAlertaDto(Long id, Long id1, Long id2, String nombreContrato, LocalDate fechaInicio, LocalDate fechaFin, long diasRestantes, boolean vencido, String s, boolean activo, boolean activo1) {
    }


}
