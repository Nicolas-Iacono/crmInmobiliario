package com.backend.crmInmobiliario.DTO.salida.prospecto;

import com.backend.crmInmobiliario.DTO.salida.UsuarioDtoSalida;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProspectoSalidaDto {
    private Long id;
    private BigDecimal rangoPrecioMin;
    private BigDecimal rangoPrecioMax;
    private Integer cantidadPersonas;
    private String zonaPreferencia;
    private Integer cantidadAmbientes;
    private Boolean cochera;
    private Boolean patio;
    private Boolean jardin;
    private Boolean pileta;
    private UsuarioDtoSalida usuarioDtoSalida;
}
