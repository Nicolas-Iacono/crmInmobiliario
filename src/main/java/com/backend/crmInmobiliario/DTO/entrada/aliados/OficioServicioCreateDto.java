package com.backend.crmInmobiliario.DTO.entrada.aliados;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OficioServicioCreateDto {
    private String titulo;
    private String descripcion;
    private BigDecimal precio;
}
