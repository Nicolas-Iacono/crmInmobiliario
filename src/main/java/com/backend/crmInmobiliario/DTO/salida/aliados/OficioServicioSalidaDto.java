package com.backend.crmInmobiliario.DTO.salida.aliados;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OficioServicioSalidaDto {
    private Long id;
    private String titulo;
    private String descripcion;
    private BigDecimal precioDesdeArs;
    private BigDecimal precio;
    private List<OficioImagenSalidaDto> imagenes;
    private boolean activo;
}

