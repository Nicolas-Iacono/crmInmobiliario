package com.backend.crmInmobiliario.DTO.entrada.contrato;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ContratoRenovacionDtoEntrada {
    private Long idContrato;
    private LocalDate nuevaFechaInicio;
    private LocalDate nuevaFechaFin;
    private Integer duracionMeses;
    private boolean mantenerGarantes = true;
    private List<Long> garantesIds;
}
