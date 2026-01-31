package com.backend.crmInmobiliario.DTO.entrada.prospecto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProspectoEntradaDto {
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private BigDecimal rangoPrecioMin;
    private BigDecimal rangoPrecioMax;
    private Integer cantidadPersonas;
    private List<String> zonaPreferencia;
    private Boolean visibilidadPublico;
    private Integer cantidadAmbientes;
    private Boolean cochera;
    private Boolean patio;
    private Boolean jardin;
    private Boolean pileta;
    private Boolean mascotas;
    private Boolean disponible;
    private Boolean destino;
}
