package com.backend.crmInmobiliario.DTO.entrada.oficios;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RegistroOficioProveedorDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String nombreCompleto;
    private String empresa;
    private String emailContacto;
    private String telefonoContacto;
    private String descripcion;
    private String localidad;
    private String provincia;
    private List<String> categorias;
    private List<String> imagenesEmpresa;
    private BigDecimal montoSuscripcionMensualArs;
}
