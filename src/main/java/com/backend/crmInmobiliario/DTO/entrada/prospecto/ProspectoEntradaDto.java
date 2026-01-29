package com.backend.crmInmobiliario.DTO.entrada.prospecto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProspectoEntradaDto {
    private BigDecimal rangoPrecioMin;
    private BigDecimal rangoPrecioMax;
    private Integer cantidadPersonas;
    private String zonaPreferencia;
    private Integer cantidadAmbientes;
    private Boolean cochera;
    private Boolean patio;
    private Boolean jardin;
    private Boolean pileta;
}
