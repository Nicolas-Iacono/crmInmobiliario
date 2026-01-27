package com.backend.crmInmobiliario.DTO.entrada.contrato;

import com.backend.crmInmobiliario.entity.EstadoContrato;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ContratoRenovacionDtoEntrada {
    private Long idContrato;
    private LocalDate nuevaFechaInicio;
    private LocalDate nuevaFechaFin;
    private Integer duracionMeses;
    private Integer actualizacion;
    private Double montoAlquiler;
    private String  montoAlquilerLetras;
    private String tipoGarantia;
    private boolean mantenerGarantes = true;
    private List<Long> garantesIds;
    private List<EstadoContrato> estados;
}