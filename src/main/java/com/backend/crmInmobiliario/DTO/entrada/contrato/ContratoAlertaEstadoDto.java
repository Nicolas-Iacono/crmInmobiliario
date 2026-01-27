package com.backend.crmInmobiliario.DTO.entrada.contrato;

import lombok.Data;

@Data
public class ContratoAlertaEstadoDto {
    private Long contratoId;
    private Long usuarioId;
    private Boolean visto;
    private Boolean noMostrar;
}
