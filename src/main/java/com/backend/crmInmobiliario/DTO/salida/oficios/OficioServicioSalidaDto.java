package com.backend.crmInmobiliario.DTO.salida.oficios;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OficioServicioSalidaDto {
    private Long id;
    private String titulo;
    private String descripcion;
    private BigDecimal precioDesdeArs;
    private BigDecimal precioHastaArs;
    private List<String> imagenesTrabajos;
}
