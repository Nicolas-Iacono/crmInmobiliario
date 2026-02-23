package com.backend.crmInmobiliario.DTO.salida.aliados;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
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




}

