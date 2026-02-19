package com.backend.crmInmobiliario.DTO.entrada.oficios;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;


@Data
@NoArgsConstructor
public class OficioServicioCreateDto {
    private Long id;
    private Long proveedorId;
    private String titulo;
    private String descripcion;
    private BigDecimal precioDesdeArs;
    private BigDecimal precio;
    private List<String> imagenes;
    private boolean activo;
}
