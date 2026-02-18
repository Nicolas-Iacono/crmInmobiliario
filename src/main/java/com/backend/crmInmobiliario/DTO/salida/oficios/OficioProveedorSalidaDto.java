package com.backend.crmInmobiliario.DTO.salida.oficios;

import lombok.Builder;
import lombok.Data;

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
    private LocalDate fechaRegistro;
    private LocalDate periodoGraciaHasta;
    private boolean enPeriodoGracia;
    private Long planId;
    private String planCode;
    private String planNombre;
    private Boolean planActivo;
    private Boolean visibleEnListado;
    private List<OficioServicioSalidaDto> servicios;
}
