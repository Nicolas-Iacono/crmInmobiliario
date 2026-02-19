package com.backend.crmInmobiliario.DTO.modificacion;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class OficioServicioUpdateDto {
    private String titulo;
    private String descripcion;
    private BigDecimal precioDesdeArs;
    private BigDecimal precio;
    private Boolean activo;
    private Boolean replaceImages;
}
