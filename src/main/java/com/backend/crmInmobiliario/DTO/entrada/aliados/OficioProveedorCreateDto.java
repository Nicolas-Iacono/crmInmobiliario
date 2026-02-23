package com.backend.crmInmobiliario.DTO.entrada.aliados;

import lombok.Data;

import java.util.List;

@Data
public class OficioProveedorCreateDto {
    private String username;
    private String password;

    private String nombreCompleto;
    private String empresa;
    private String emailContacto;
    private String telefonoContacto;

    private String descripcion;
    private String localidad;
    private String provincia;

    private List<String> categorias;
}
