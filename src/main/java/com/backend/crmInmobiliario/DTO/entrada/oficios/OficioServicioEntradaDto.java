package com.backend.crmInmobiliario.DTO.entrada.oficios;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OficioServicioEntradaDto {
    private String titulo;
    private String descripcion;
    private BigDecimal precioDesdeArs;
    private BigDecimal precioHastaArs;
}
