package com.backend.crmInmobiliario.DTO.salida.oficios;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class OficioProveedorSalidaDto {
    private Long id;
    private String nombreCompleto;
    private String empresa;
    private String emailContacto;
    private String telefonoContacto;
    private String descripcion;
    private String localidad;
    private String provincia;
    private List<String> categorias;
    private List<String> imagenesEmpresa;
    private Double promedioCalificacion;
    private Integer totalCalificaciones;
    private Boolean suscripcionActiva;
    private LocalDate suscripcionVenceEl;
    private BigDecimal montoSuscripcionMensualArs;
    private List<OficioServicioSalidaDto> servicios;
}
