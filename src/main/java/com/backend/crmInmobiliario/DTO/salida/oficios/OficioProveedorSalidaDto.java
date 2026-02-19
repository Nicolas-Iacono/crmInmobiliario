package com.backend.crmInmobiliario.DTO.salida.oficios;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
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
    private String imagenPerfilUrl;
    private Double promedioCalificacion;
    private Integer totalCalificaciones;
    private Long planId;
    private boolean planActivo;
}
