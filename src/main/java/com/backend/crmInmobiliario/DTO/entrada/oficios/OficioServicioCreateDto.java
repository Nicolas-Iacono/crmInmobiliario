package com.backend.crmInmobiliario.DTO.entrada.oficios;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class OficioServicioCreateDto {
    private String titulo;
    private String descripcion;
    private BigDecimal precioDesdeArs;
    private BigDecimal precio;
}
