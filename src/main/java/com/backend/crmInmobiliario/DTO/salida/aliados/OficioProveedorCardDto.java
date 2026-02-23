package com.backend.crmInmobiliario.DTO.salida.aliados;

import lombok.Data;

import java.util.List;

@Data
public class OficioProveedorCardDto {
    private Long id;
    private String nombreCompleto;
    private String empresa;
    private String descripcion;
    private String telefonoContacto;
    private String emailContacto;
    private String provincia;
    private String localidad;

    // Foto perfil
    private Long imagenPerfilId;
    private String imagenPerfilUrl;

    // Métricas
    private Double promedioCalificacion;
    private Integer totalCalificaciones;

    private List<String> categorias;
}
