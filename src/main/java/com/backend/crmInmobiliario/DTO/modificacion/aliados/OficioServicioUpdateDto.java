package com.backend.crmInmobiliario.DTO.modificacion.aliados;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class OficioServicioUpdateDto {
    private String titulo;
    private String descripcion;
    private BigDecimal precio;
    private Boolean activo;
}
