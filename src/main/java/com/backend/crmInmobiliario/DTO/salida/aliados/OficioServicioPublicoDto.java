package com.backend.crmInmobiliario.DTO.salida.aliados;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OficioServicioPublicoDto {
    private Long id;
    private String titulo;
    private String descripcion;
    private BigDecimal precio;
    private boolean activo;
    private List<OficioImagenSalidaDto> imagenes;
}
